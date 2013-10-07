package thirdparty.delaunay;

/*
 * Copyright (c) 2007 by L. Paul Chew.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Triangle is an immutable Set of exactly three Pnts.
 *
 * All Set operations are available. Individual vertices can be accessed via
 * iterator() and also via triangle.get(index).
 *
 * Note that, even if two triangles have the same vertex set, they are
 * *different* triangles. Methods equals() and hashCode() are consistent with
 * this rule.
 *
 * @author Paul Chew
 *
 * Created December 2007. Replaced general simplices with geometric triangle.
 *
 */
public class Triangle extends ArraySet<Point> {

    private int idNumber;                   // The id number
    private Point circumcenter = null;        // The triangle's circumcenter

    private static int idGenerator = 0;     // Used to create id numbers
    public static boolean moreInfo = false; // True iff more info in toString

    /**
     * @param vertices the vertices of the Triangle.
     * @throws IllegalArgumentException if there are not three distinct vertices
     */
    public Triangle (Point... vertices) {
        this(Arrays.asList(vertices));
    }

    /**
     * @param collection a Collection holding the Simplex vertices
     * @throws IllegalArgumentException if there are not three distinct vertices
     */
    public Triangle (Collection<? extends Point> collection) {
        super(collection);
        idNumber = idGenerator++;
        if (this.size() != 3)
            throw new IllegalArgumentException("Triangle must have 3 vertices");
    }

    @Override
    public String toString () {
        if (!moreInfo) return "Triangle" + idNumber;
        return "Triangle" + idNumber + super.toString();
    }

    /**
     * Get arbitrary vertex of this triangle, but not any of the bad vertices.
     * @param badVertices one or more bad vertices
     * @return a vertex of this triangle, but not one of the bad vertices
     * @throws NoSuchElementException if no vertex found
     */
    public Point getVertexButNot (Point... badVertices) {
        Collection<Point> bad = Arrays.asList(badVertices);
        for (Point v: this) if (!bad.contains(v)) return v;
        throw new NoSuchElementException("No vertex found");
    }

    /**
     * True iff triangles are neighbors. Two triangles are neighbors if they
     * share a facet.
     * @param triangle the other Triangle
     * @return true iff this Triangle is a neighbor of triangle
     */
    public boolean isNeighbor (Triangle triangle) {
        int count = 0;
        for (Point vertex: this)
            if (!triangle.contains(vertex)) count++;
        return count == 1;
    }

    /**
     * Report the facet opposite vertex.
     * @param vertex a vertex of this Triangle
     * @return the facet opposite vertex
     * @throws IllegalArgumentException if the vertex is not in triangle
     */
    public ArraySet<Point> facetOpposite (Point vertex) {
        ArraySet<Point> facet = new ArraySet<Point>(this);
        if (!facet.remove(vertex))
            throw new IllegalArgumentException("Vertex not in triangle");
        return facet;
    }

    /**
     * @return the triangle's circumcenter
     */
    public Point getCircumcenter () {
        if (circumcenter == null)
            circumcenter = Point.circumcenter(this.toArray(new Point[0]));
        return circumcenter;
    }

    /* The following two methods ensure that a Triangle is immutable */

    @Override
    public boolean add (Point vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Point> iterator () {
        return new Iterator<Point>() {
            private Iterator<Point> it = Triangle.super.iterator();
            public boolean hasNext() {return it.hasNext();}
            public Point next() {return it.next();}
            public void remove() {throw new UnsupportedOperationException();}
        };
    }

    /* The following two methods ensure that all triangles are different. */

    @Override
    public int hashCode () {
        return (int)(idNumber^(idNumber>>>32));
    }

    @Override
    public boolean equals (Object o) {
        return (this == o);
    }

}