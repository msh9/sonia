/* This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package sonia.tester;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import sonia.LayoutSlice;
import sonia.NetUtils;
import sonia.SoniaController;
import sonia.SoniaLayoutEngine;
import sonia.Subnet;
import sonia.settings.LayoutSettings;

public class Tester {

	/**
	 * loads a set of networks and compares times to calc apsp
	 * 
	 * @author skyebend
	 * @param args
	 */
	
	private String fileName = "spTest.txt";
	private String errorName = "spTest.error.txt";

	private String[] filesToTry = {
			"C:/Documents and Settings/skyebend/Desktop/2000matrix.dl"
			//,"C:/Documents and Settings/skyebend/Desktop/soniaStuff/cls33_10_16_96.son"
	//};
			//,"C:/Documents and Settings/skyebend/Desktop/soniaStuff/test60.dl"
			//,"C:/Documents and Settings/skyebend/Desktop/ATA/clauset/schedule00/schdeule00ShortTime.net"
	//,"C:/Documents and Settings/skyebend/Desktop/fecdata/committeeProcessing/SenatorComContribPartyMay06.son"
	};

	private int reps = 1;

	public Tester() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		
		out.write("nodes\tnormal\tfast\tfastFast\tequal\n");

		for (int f = 0; f < filesToTry.length; f++) {
			for (int r = 0; r < reps; r++) {

				// create a instance of sonia
				SoniaController cont = new SoniaController(1);
				System.out.println("reading: "+filesToTry[f]);
				cont.loadFile(filesToTry[f]);
				// do the slice settings
				LayoutSettings settings = new LayoutSettings();
				settings.put(LayoutSettings.SLICE_END, "10.0");
				settings.put(LayoutSettings.SLICE_DELTA, "10.0");
				settings.put(LayoutSettings.SLICE_DURATION, "10.0");
				settings.put(LayoutSettings.SLICE_START, "0.0");
				settings.put(LayoutSettings.SLICE_AGGREGATION,
						"Number  of i->j ties");
				settings.put(LayoutSettings.ANIMATE_TYPE, "cosine animation");
				settings.put(LayoutSettings.LAYOUT_TYPE, "MultiComp KK Layout");
				/*
				 * SettingsClass=sonia.settings.LayoutSettings
				 * SLICE_AGGREGATION=Number of i->j ties SLICE_DELTA=10
				 * SLICE_DURATION=10 SLICE_START=0.0 SLICE_END=10
				 * ANIMATE_TYPE=cosine animation LAYOUT_TYPE=MultiComp KK Layout
				 */

				cont.createLayout(settings);
				SoniaLayoutEngine eng = cont.getEngine(0);
				LayoutSlice slice = eng.getCurrentSlice();
				IntArrayList includeAll = new IntArrayList(slice
						.getMaxNumNodes());
				for (int i = 0; i < slice.getMaxNumNodes(); i++) {
					includeAll.add(i);
				}
				Subnet subnet = new Subnet(NetUtils.getSymMaxMatrix(slice),
						includeAll);
				slice = null;
				
				// get Matrix from subnet and make it into disimliarity
				// (matrix was symetrized when divided into components)
				// using the max and min value of all matricies in engine
				// then sets up the matrix of path distances with Dijkstras APSP
				
//				System.out.println("starting fast");
//				long fastStart = System.currentTimeMillis();
//				DenseDoubleMatrix2D distMatrix2 = NetUtils
//						.getFastAllShortPathMatrix(subnet
//								.getMatrix(), eng.getMaxMatrixVal(), eng
//								.getMinMatrixValue(),false);
//				long fastEnd = System.currentTimeMillis();
//				
				System.out.println("starting fast fast");
				long fastFastStart = System.currentTimeMillis();
				DenseDoubleMatrix2D distMatrix3 = NetUtils
						.getFastFastAllShortPathMatrix(subnet
								.getMatrix(), eng.getMaxMatrixVal(), eng
								.getMinMatrixValue(),false);
				long fastFastEnd = System.currentTimeMillis();
				
//				System.out.println("starting normal");
//				long normalStart = System.currentTimeMillis();
//				DenseDoubleMatrix2D distMatrix1 = NetUtils
//						.getAllShortPathMatrix(NetUtils.getReverse(subnet
//								.getMatrix(), eng.getMaxMatrixVal(), eng
//								.getMinMatrixValue()));
//				long normalEnd = System.currentTimeMillis();

				// did they give the same result?
//				boolean equal = distMatrix1.equals(distMatrix3);
//				if (equal){
//					equal = distMatrix2.equals(distMatrix3);
//				}
				// print times

				//if they are differnt, print them
//				if (!equal){
//					System.out.println("matrices do not match, printing to files...");
//					BufferedWriter error1 = new BufferedWriter(new FileWriter("spErrorNormal.txt"));
//					error1.write("normal:"+distMatrix1);
//					error1.flush();
//					error1.close();
//					BufferedWriter error2 = new BufferedWriter(new FileWriter("spErrorFast.txt"));
//					error2.write("fast:"+distMatrix3);
//					error2.flush();
//					error2.close();
//					System.out.println("norm::"+distMatrix1.viewRow(0));
//					System.err.println("fast:"+distMatrix3.viewRow(0));
//					
//				}
//				out.write(distMatrix1.rows() + "\t"
//						+ (normalEnd - normalStart)
//					//	+ "\t" + (fastEnd - fastStart)
//						+ "\t\t" +(fastFastEnd -fastFastStart)+
//						"\t"+equal+"\n");
			}
		}
		out.flush();
		out.close();
		System.out.println("done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Tester tester = new Tester();

	}

}
