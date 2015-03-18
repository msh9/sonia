package sonia.mapper;


import java.awt.Shape;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import sonia.ShapeFactory;

/**
 * Creates an arbitrary mapping of objects to shapes, looping over all the shapes available from the ShapeFactory
 * @author skyebend
 *
 */
public class DefaultShapes extends Shapemapper {
	
	public static String MAPPER_NAME = "Default Shapes";
	
	private HashMap<Object, Shape> shapemap = new HashMap<Object, Shape>();

	public void createMapping(Set<Object> values) {
		shapemap.clear();
		//figure out how many shapes
		int valcount = 0;
		Iterator<Object> valiter = values.iterator();
		while (valiter.hasNext()){
			
			Object value = valiter.next();
			//keep recycling the shapes for the values
			//TODO should we skip the blank shapes?
			String shapeName = ShapeFactory.shapeNames[valcount % ShapeFactory.shapeNames.length];
			try {
				shapemap.put(value, ShapeFactory.getShapeFor(shapeName));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			valcount++;
			
		}
		

	}

	public Shape getShapeFor(Object value) {
		return shapemap.get(value);
	}

	public void mapShape(Object value, Shape shape) {
		shapemap.put(value, shape);

	}

	public Set<Object> getValues() {
		return shapemap.keySet();
	}

	@Override
	public String getMapperName() {
		return MAPPER_NAME;
	}

	

}
