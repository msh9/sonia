package sonia.analytics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;






import sonia.NetUtils;
import sonia.NodeClusterAttribute;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;

import cern.colt.function.IntIntDoubleFunction;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;


/**
 * This class was imported with minor revisions from the Prefuse project, originally by 
 * <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 * 
 * availible from http://sourceforge.net/cvs/?group_id=98962
 * 
 * The code is distributed under BSD license, with the following license text
 * --------------
 * Copyright (c) 2004, 2005 Regents of the University of California.
 *   All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  
 *  3. All advertising materials mentioning features or use of this software
 *  must display the following acknowledgement:
 *  
 *  This product includes software developed by the Group for User 
 *  Interface Research at the University of California at Berkeley.
 *  
 *  4. The name of the University may not be used to endorse or promote products 
 *  derived from this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 *  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *   OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *    SUCH DAMAGE.
 * 
 * Computes the community structure of an input graph using the
 * algorithm due to Newman. This algorithm uses a hierarchical
 * agglomerative clustering approach to greedily optimize a metric
 * of network modularity or community clustering.
 * 
 * The output of the algorithm is a list of the successive values
 * of the community metric at each iteration of the algorithm, and
 * a record of each agglomeration, allowing the cluster tree to be
 * later reconstructed.
 * 
 * Nov 19, 2004 - jheer - Created class
 *
 */
public class ModularityStructureDirected  {

    private double dQ, maxDQ = 0.0;
    private int x, y;
    private int[] maxEdge = new int[] {0,0};
    
    private DoubleMatrix2D e;
    private LinkedList E;
    private DoubleMatrix1D a;
    
    private boolean verbose = true;
    private List plist;
    private double[] qval;
    

    
    
    public void runClustering(DoubleMatrix2D g) {
    
        long timein = System.currentTimeMillis();
        
        int N = g.rows();
        double Q = 0, maxQ = 0, maxIter = 0;
        
        plist = new ArrayList(N-1);
        qval = new double[N-1];
        
        if (verbose) System.out.println("Weighting matrix");
        
        // initialize weighted matrix
        e = g.copy();
        if (verbose) System.out.println("\tComputing normalization factor");
        e.forEachNonZero(new ClearDiagonal());
        ZSum zSum = new ZSum();
        e.forEachNonZero(zSum);
        if (verbose) System.out.println("\tScaling matrix");
        e.forEachNonZero(new Mult(1/zSum.getSum()));
        
        if (verbose) System.out.println("Computing column sums");
        
        // initialize column sums
        a = new DenseDoubleMatrix1D(N);
        e.forEachNonZero(new RowSum());
        
        if (verbose) System.out.print("Collecting edges... ");
        
        // initialize edges
        E = new LinkedList();
        e.forEachNonZero(new EdgeCollector());
        if (verbose) System.out.println(E.size()+" edges");
        
        if (verbose) System.out.println("Starting clustering");
        
        for ( int i=0; i < N-1 && E.size() > 0; i++ ) {
            maxDQ = Double.NEGATIVE_INFINITY;
            maxEdge[0] = 0; maxEdge[1] = 0;
            
            Iterator iter = E.iterator();
            while ( iter.hasNext() ) {
                int[] edge = (int[])iter.next();
                x = edge[0]; y = edge[1];
                if ( x == y ) continue;
                // compute delta Q
                dQ = e.getQuick(x,y) + e.getQuick(y,x) 
                        - 2*a.getQuick(x)*a.getQuick(y);
                // check against max so far
                if ( dQ > maxDQ ) {
                    maxDQ = dQ;
                    maxEdge[0] = x; maxEdge[1] = y;
                }
            }
            
            // update the graph
            x = maxEdge[0]; y = maxEdge[1];
            if ( y < x ) { // ensure merge ordering to lower index
                int tmp = y; y = x; x = tmp;
            }
            double na = 0.0;
            for ( int k=0; k < N; k++ ) {
                double v = e.getQuick(x,k) + e.getQuick(y,k);
                if ( v != 0 ) { 
                    na += v;
                    e.setQuick(x,k,v);
                    e.setQuick(y,k,0);
                }
            }
            for ( int k=0; k < N; k++ ) {
                double v = e.getQuick(k,x) + e.getQuick(k,y);
                if ( v != 0 ) {
                    e.setQuick(k,x,v);
                    e.setQuick(k,y,0);
                }
            }
            a.setQuick(x,na);
            a.setQuick(y,0.0);
            
            if ( i % 100 == 0 ) {
                e.trimToSize();
            }
            
            // update edge list
            iter = E.iterator();
            while ( iter.hasNext() ) {
                int[] edge = (int[])iter.next();
                if ( (edge[0]==x && edge[1]==y) || (edge[0]==y && edge[1]==x) ) {
                    iter.remove();
                } else if ( edge[0] == y ) {
                    edge[0] = x;
                } else if ( edge[1] == y ) {
                    edge[1] = x;
                }
            }
            
            Q += maxDQ;
            if ( Q > maxQ ) {
                maxQ = Q;
                maxIter = i+1;
            }
            
            qval[i] = Q;
            //record the ends of edges removed
            plist.add(new int[] {x+1,y+1}); // shift back from 0-base to 1-base
            
            if (verbose) System.out.println(Q+"\t"+"iter "+(i+1)+"("+(N-i-1)+")\t"+"nedges = "+E.size());
        }
        
        if (verbose) System.out.println();
        if (verbose) System.out.println("maxQ = "+maxQ+", at iter "+maxIter+" (-"+(N-maxIter)+")");
        if (verbose) System.out.println(((System.currentTimeMillis()-timein)/1000.0)+" seconds");
        
 
    } //
    
    
    
    public List getMergeList() {
        return plist;
    } //
    
    public double[] getQValues() {
        return qval;
    } //
    
    // -- helpers -------------------------------------------------------------
    
    public class ClearDiagonal implements IntIntDoubleFunction {
        public double apply(int arg0, int arg1, double arg2) {
            return ( arg0 == arg1 ? 0.0 : arg2 );
        }
    } //
    
    public class EdgeCollector implements IntIntDoubleFunction {
        public double apply(int arg0, int arg1, double arg2) {
            if ( arg0 != arg1 ) {
                int[] edge = new int[] {arg0,arg1};
                E.add(edge);
            }
            return arg2;
        }
    } //
    
    public class RowSum implements IntIntDoubleFunction {
        public double apply(int arg0, int arg1, double arg2) {
            a.setQuick(arg0, a.getQuick(arg0)+arg2);
            return arg2;
        }
    } //
    
    public class Mult implements IntIntDoubleFunction {
        private double scalar;
        public Mult(double s) {
            scalar = s;
        }
        public double apply(int arg0, int arg1, double arg2) {
            return arg2*scalar;
        }
    } //
    
    public class ZSum implements IntIntDoubleFunction {
        double sum = 0;
        public double apply(int arg0, int arg1, double arg2) {
            sum += arg2;
            return arg2;
        }
        public void reset() {
            sum = 0;
        }
        public double getSum() {
            return sum;
        }
    } 
    
   
    public int getMaxQValueIndex() {
        // get index for "optimal" cut
        int idx = -1;
        double max = -1;
        if (qval != null){
        for ( int i=0; i<qval.length; i++ ) {
            if ( qval[i] > max ) {
                max = qval[i];
                idx = i;
            }
        }
        }
        return idx;
    } //
    
    
    /*
     * needs to know the idx of the step with the highest modularity
     * uses start and end time for start and end of clusters
     */
    public Vector makeClustersFor(int idx,double[] qvals, List mergeList,
    		double startTime, double endTime) {

	        Vector<NodeClusterAttribute> clusters = new Vector<NodeClusterAttribute>();
	        
	        // merge groups
	        int i = 0;
	        // use link hashed map to enforce ordering
	        // this is crucial for getting stable colors
	        LinkedHashMap merge = new LinkedHashMap();
	        Iterator iter = mergeList.iterator();
	        while ( iter.hasNext() && i <= idx ) {
	            int[] edge = (int[])iter.next();
	            Integer k1 = new Integer(edge[0]);
	            Integer k2 = new Integer(edge[1]);
	            IntArrayList l1;
	            //check if first node is already in a component
	            //if not, put it in its on list and add
	            if ( (l1=(IntArrayList)merge.get(k1)) == null ) {
	                l1 = new IntArrayList();
	                l1.add(k1);
	                merge.put(k1,l1);
	            }
	            //check if second node is in component
	            IntArrayList l2;
	            if ( (l2=(IntArrayList)merge.get(k2)) == null ) {
	                l1.add(k2);
	            } else {
	                l1.addAllOf(l2);
	                merge.remove(k2);
	            }
	            i++;

	        }
	        // set community count
	      //  numCommunities = merge.size();
	        //System.out.println("numcomm = "+this.numCommunities);
	        
	        
	        // change format of community groups
	        int id = 0;
	        iter = merge.keySet().iterator(); //looping across clusters
	        while ( iter.hasNext() ) {
	        	IntArrayList l = (IntArrayList)merge.get(iter.next());
	        	//put the list of nodes into a cluster attribute
	        	NodeClusterAttribute cluster = new NodeClusterAttribute(id+"",startTime,endTime,l);
	        	cluster.setFillColor(Color.orange);
	            clusters.add(cluster);
	            id++;
	        }
        
	   
	        return (clusters);
    } //

    
    //debug testing
    public static void main(String[] args){
    	String filename = args[0];
    	//make a sonia
    	SoniaController tester = new SoniaController(0);
    	tester.loadFile(filename);
    	tester.createNewLayout();
    	//load an external file
    	SoniaLayoutEngine eng = tester.getEngine(0);
    	DoubleMatrix2D matrix =  NetUtils.getMatrix(eng.getSlice(0));
    	//debug
    	System.out.println(matrix);
    	ModularityStructureDirected modularity = new ModularityStructureDirected();
    	modularity.verbose = true;
    	modularity.runClustering(matrix);
    	//debug
    	System.out.println(modularity.getMergeList().toArray());
    	
    }
    
} // end of class CommunityStructureDirected
