package com.github.dakusui.geophile;

import com.geophile.z.SpatialJoin;
import com.geophile.z.index.RecordWithSpatialObject;
import com.geophile.z.index.tree.TreeIndex;

import java.util.Comparator;

/**
 * A suite of constants, classes, an interfaces that models data structures and constraints among them in an application
 * of geophile.
 */
public interface DataModel {
    interface SpatialObject extends com.geophile.z.SpatialObject {
        class Point extends com.geophile.z.spatialobject.d2.Point implements SpatialObject {
            /**
             * Creates a point at (x, y).
             *
             * @param x The x coordinate.
             * @param y The y coordinate.
             */
            public Point(double x, double y) {
                super(x, y);
            }
        }

        class Box extends com.geophile.z.spatialobject.d2.Box implements SpatialObject {
            public Box(double xLo, double xHi, double yLo, double yHi) {
                super(xLo, xHi, yLo, yHi);
            }

            public boolean contains(Point point) {
                Box box = this;
                return box.xLo() <= point.x() && point.x() <= box.xHi() &&
                        box.yLo() <= point.y() && point.y() <= box.yHi();
            }

            public boolean overlaps(Box b) {
                Box a = this;
                return a.xLo() < b.xHi() && b.xLo() < a.xHi() &&
                        a.yLo() < b.yHi() && b.yLo() < a.yHi();
            }
        }
    }

    abstract class Record extends RecordWithSpatialObject {
        public static class Immutable extends Record {
            private final int id;

            Immutable(SpatialObject spatialObject, int id) {
                spatialObject(spatialObject);
                this.id = id;
            }

            @Override
            int id() {
                return this.id;
            }
        }

        public static class Mutable extends Record {
            private int id;

            Mutable() {
            }

            public Mutable(SpatialObject spatialObject, int id) {
                spatialObject(spatialObject);
                this.id = id;
            }

            @Override
            int id() {
                return this.id;
            }
        }

        abstract int id();

        @Override
        public void copyTo(com.geophile.z.Record record) {
            if (record instanceof Mutable) {
                ((Mutable) record).id = this.id();
                super.copyTo(record);
                return;
            }
            throw new UnsupportedOperationException("Non mutable recode '" + record.getClass() + ":" + record + "' was given.");
        }

        public static class Builder implements com.geophile.z.Record.Factory<Record> {
            private final boolean stable;
            private SpatialObject spatialObject;
            private int id;

            public Builder(boolean stable) {
                this.stable = stable;
            }

            public Builder with(SpatialObject spatialObject) {
                this.spatialObject = spatialObject;
                return this;
            }

            @Override
            public Record newRecord() {
                if (stable) {
                    return new Record.Immutable(this.spatialObject, this.id++);
                } else {
                    return new Record.Mutable(this.spatialObject, this.id++);
                }
            }
        }
    }

    class Index extends TreeIndex<Record> {
        private static final Comparator<Record> COMPARATOR =
                new Comparator<Record>() {
                    @Override
                    public int compare(Record r, Record s) {
                        return
                                r.z() < s.z() ?
                                        -1 :
                                        r.z() > s.z() ?
                                                1 :
                                                r.id() < s.id() ?
                                                        -1 :
                                                        r.id() > s.id() ?
                                                                1 :
                                                                0;
                    }
                };

        public Index(boolean stableRecords) {
            super(COMPARATOR, stableRecords);
        }

        @Override
        public Record newRecord() {
            return new Record.Mutable();
        }
    }

    class SpatialJoinFilter implements SpatialJoin.Filter<RecordWithSpatialObject, RecordWithSpatialObject> {
        public static SpatialJoinFilter INSTANCE = new SpatialJoinFilter();

        @Override
        public boolean overlap(RecordWithSpatialObject record1, RecordWithSpatialObject record2) {
            assert record1 != null && record1.spatialObject() instanceof SpatialObject;
            assert record2 != null && record2.spatialObject() instanceof SpatialObject;

            SpatialObject a = (SpatialObject) record1.spatialObject();
            SpatialObject b = (SpatialObject) record2.spatialObject();
            if (b instanceof SpatialObject.Point) {
                SpatialObject tmp = a;
                a = b;
                b = tmp;
            }
            if (a instanceof SpatialObject.Point) {
                if (b instanceof SpatialObject.Point) {
                    return a.equals(b);
                } else {
                    return ((SpatialObject.Box) b).contains((SpatialObject.Point) a);
                }
            }
            return ((SpatialObject.Box) a).overlaps((SpatialObject.Box) b);
        }
    }
}
