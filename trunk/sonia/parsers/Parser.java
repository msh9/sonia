package sonia.parsers;

/**
 * <p>Title:SoNIA (Social Network Image Animator) </p>
 * <p>Description:Animates layouts of time-based networks
 * <p>Copyright: CopyLeft  2004: GNU GPL</p>
 * <p>Company: none</p>
 * @author Skye Bender-deMoll unascribed
 * @version 1.1
 */


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

import java.util.Vector;
import java.util.Hashtable;
import java.io.IOException;


/**
 * There are multiple data formats available for storing and exchanging network
 * data, with varying degrees of use, human code-ability, features, flexibility,
 *  etc.  So far, none of them seem widely accepted or standard.  Unfortunately,
 *  our focus on time-based data seemed inappropriate for most of the existing
 * formats (with the possible exception of the GraphXML approach) especially
 * since our desire was to make a format useable by non-programmers so they
 * could import their existing data with a minimum of reformatting.  So we have
 * created yet another graph input format (.son), but hope to eventually support other
 *  existing formats as a file interchange option.  And perhaps and SQL backend?
 */
public interface Parser
{
  /**
   * Asks the parser to read a network from the file indicated by the passed string
   * @param fileAndPath String giving the path to the file and filename
   * @throws IOException if an error is encounterd parsing the file.
   * must give very specific descriptive error and line number
   */
  public void parseNetwork(String fileAndPath) throws IOException;

  /**
   * Returns the maximum number of unique nodes (not NodeEvents) so that the size
   * of the network matricies can be determined
   */
  public int getMaxNumNodes();

  /**
   * Returns the total number of NodeEvents parsed and created
   */
  public int getNumNodeEvents();

  /**
   * Returns the total number of ArcEvents parsed and created
   */
  public int getNumArcEvents();

  /**
   * Returns a vector containing all the parsed NodeEvents
   */
  public Vector getNodeList();

  /**
   * Returns a vector containing all the parsed ArcEvents
   */
  public Vector getArcList();

  /**
   * Returns a string containing any additional info about the parsed file,
   * such as comments (if supported)
   */
  public String getNetInfo();

  /**
   * Returns a string with the name of the parser
   */
  public String getParserInfo();

}
