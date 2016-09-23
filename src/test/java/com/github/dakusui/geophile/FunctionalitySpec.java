package com.github.dakusui.geophile;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Iterators.toArray;
import static com.google.common.collect.Iterators.transform;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(checker = @Checker(SmartConstraintChecker.class))
public class FunctionalitySpec {
    public enum SpaceFactory {
        @SuppressWarnings("unused")NORMAL(new double[]{0, 0}, new double[]{1_000_000, 1_000_000}, new int[]{20, 20}, null),
        @SuppressWarnings("unused")SMALL(new double[]{0, 0}, new double[]{300, 300}, new int[]{20, 20}, null);

        private final int[] interleave;
        private final int[] gridBits;
        private final double[] hi;
        private final double[] lo;

        SpaceFactory(double[] hi, double[] lo, int[] gridBits, int[] interleave) {
            this.hi = hi;
            this.lo = lo;
            this.gridBits = gridBits;
            this.interleave = interleave;
        }

        public Space create() {
            if (interleave == null) {
                return Space.newSpace(this.hi, this.lo, this.gridBits);
            }

            return Space.newSpace(this.hi, this.lo, this.gridBits, this.interleave);
        }
    }

    public enum SpatialObjectSetProvider {
        @SuppressWarnings("unused")EMPTY {
            @Override
            public Iterable<? extends DataModel.SpatialObject> get() {
                return emptyList();
            }

            @Override
            public DataModel.SpatialObject pointHitsNone() {
                return new DataModel.SpatialObject.Point(0, 0);
            }
        },
        @SuppressWarnings("unused")ONLY_ONE_BOX {
            @Override
            public Iterable<? extends DataModel.SpatialObject> get() {
                return singletonList(new DataModel.SpatialObject.Box(4, 4, 10, 10));
            }

            @Override
            public DataModel.SpatialObject pointHitsNone() {
                return new DataModel.SpatialObject.Point(1, 5);
            }

            @Override
            public DataModel.SpatialObject pointHitsOne() {
                return new DataModel.SpatialObject.Point(5, 5);
            }


            @Override
            public DataModel.SpatialObject boxHitsNone() {
                return new DataModel.SpatialObject.Box(5, 15, 15, 25);
            }

            @Override
            public DataModel.SpatialObject boxHitsOne() {
                return new DataModel.SpatialObject.Box(9, 9, 11, 11);
            }

            @Override
            public DataModel.SpatialObject boxHitsAll() {
                return new DataModel.SpatialObject.Box(1, 1, 11, 11);
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsOne() {
                return get();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsOne() {
                return get();
            }

        },
        @SuppressWarnings("unused")MULTIPLE_BOXES {
            @Override
            public Iterable<? extends DataModel.SpatialObject> get() {
                return asList(
                        new DataModel.SpatialObject.Box(10, 10, 20, 20),
                        new DataModel.SpatialObject.Box(5, 15, 25, 35)
                );
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsSome() {
                return get();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsOne() {
                return singletonList(Iterables.get(get(), 1));
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsSome() {
                return get();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsOne() {
                return singletonList(Iterables.get(get(), 1));
            }

            @Override
            public DataModel.SpatialObject pointHitsNone() {
                return new DataModel.SpatialObject.Point(9, 9);
            }

            @Override
            public DataModel.SpatialObject pointHitsOne() {
                return new DataModel.SpatialObject.Point(11, 11);
            }

            @Override
            public DataModel.SpatialObject boxHitsOne() {
                return new DataModel.SpatialObject.Box(9, 9, 11, 11);
            }

            @Override
            public DataModel.SpatialObject boxHitsAll() {
                return new DataModel.SpatialObject.Box(1, 1, 11, 11);
            }

        },
        @SuppressWarnings("unused")ONLY_ONE_POINT {
            @Override
            public Iterable<? extends DataModel.SpatialObject> get() {
                return singletonList(new DataModel.SpatialObject.Point(1, 1));
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsOne() {
                return this.get();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsOne() {
                return this.get();
            }

            @Override
            public DataModel.SpatialObject pointHitsOne() {
                return new DataModel.SpatialObject.Point(1, 1);
            }

            @Override
            public DataModel.SpatialObject pointHitsNone() {
                return new DataModel.SpatialObject.Point(0, 0);
            }

            @Override
            public DataModel.SpatialObject boxHitsNone() {
                return new DataModel.SpatialObject.Box(2, 2, 2.5, 2.5);
            }

            @Override
            public DataModel.SpatialObject boxHitsOne() {
                return new DataModel.SpatialObject.Box(0.5, 0.5, 1.5, 1.5);
            }


            @Override
            public DataModel.SpatialObject boxHitsAll() {
                return new DataModel.SpatialObject.Box(0.5, 0.5, 1.5, 1.5);
            }
        },
        @SuppressWarnings("unused")MULTIPLE_POINTS {
            @Override
            public Iterable<? extends DataModel.SpatialObject> get() {
                return asList(
                        new DataModel.SpatialObject.Point(1, 2),
                        new DataModel.SpatialObject.Point(100, 200)
                );
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsSome() {
                return get();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsOne() {
                return singletonList(Iterables.get(get(), 1));
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsSome() {
                return get();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsOne() {
                return singletonList(Iterables.get(get(), 1));
            }

            @Override
            public DataModel.SpatialObject pointHitsOne() {
                return new DataModel.SpatialObject.Point(100, 200);
            }

            @Override
            public DataModel.SpatialObject pointHitsNone() {
                return new DataModel.SpatialObject.Point(3, 4);
            }

            @Override
            public DataModel.SpatialObject boxHitsNone() {
                return new DataModel.SpatialObject.Box(3, 4, 30, 40);
            }

            @Override
            public DataModel.SpatialObject boxHitsOne() {
                return new DataModel.SpatialObject.Box(99, 199, 101, 201);
            }

            @Override
            public DataModel.SpatialObject boxHitsSome() {
                return new DataModel.SpatialObject.Box(0, 1, 101, 201);
            }

            @Override
            public DataModel.SpatialObject boxHitsAll() {
                return new DataModel.SpatialObject.Box(0, 1, 101, 201);
            }
        },
        @SuppressWarnings("unused")MIXED {
            @Override
            public Iterable<? extends DataModel.SpatialObject> get() {
                return Iterables.concat(
                        MULTIPLE_BOXES.get(),
                        MULTIPLE_POINTS.get()
                );
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsOne() {
                return MULTIPLE_POINTS.expectedResultForPointHitsOne();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsSome() {
                return MULTIPLE_POINTS.expectedResultForPointHitsSome();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsOne() {
                return MULTIPLE_BOXES.expectedResultForBoxHitsOne();
            }

            @Override
            protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsSome() {
                return MULTIPLE_BOXES.expectedResultForBoxHitsSome();
            }

            @Override
            public DataModel.SpatialObject pointHitsNone() {
                return MULTIPLE_POINTS.pointHitsNone();
            }

            @Override
            public DataModel.SpatialObject pointHitsOne() {
                return MULTIPLE_BOXES.pointHitsOne();
            }

            @Override
            public DataModel.SpatialObject pointHitsSome() {
                return MULTIPLE_BOXES.pointHitsSome();
            }

            @Override
            public DataModel.SpatialObject boxHitsNone() {
                return MULTIPLE_BOXES.pointHitsNone();
            }

            @Override
            public DataModel.SpatialObject boxHitsOne() {
                return MULTIPLE_BOXES.boxHitsOne();
            }

            @Override
            public DataModel.SpatialObject boxHitsSome() {
                return MULTIPLE_BOXES.boxHitsSome();
            }

            @Override
            public DataModel.SpatialObject boxHitsAll() {
                return MULTIPLE_BOXES.boxHitsAll();
            }
        },;

        abstract public Iterable<? extends DataModel.SpatialObject> get();

        public Iterable<? extends DataModel.SpatialObject> expectedResultFor(QueryProvider queryProvider) {
            if (queryProvider.get(this) == null) return null;
            switch (queryProvider) {
                case POINT_HITS_NONE:
                case BOX_HITS_NONE:
                    return emptyList();
                case BOX_HITS_ALL:
                    return this.get();
                case POINT_HITS_ONE:
                    return this.expectedResultForPointHitsOne();
                case POINT_HITS_SOME:
                    return this.expectedResultForPointHitsSome();
                case BOX_HITS_ONE:
                    return this.expectedResultForBoxHitsOne();
                case BOX_HITS_SOME:
                    return this.expectedResultForBoxHitsSome();
            }
            throw new RuntimeException(format("Expectation for %s is not defined. (%s)", queryProvider, this));
        }

        public abstract DataModel.SpatialObject pointHitsNone();

        public DataModel.SpatialObject pointHitsOne() {
            return null;
        }

        public DataModel.SpatialObject pointHitsSome() {
            return null;
        }

        public DataModel.SpatialObject boxHitsNone() {
            return null;
        }

        public DataModel.SpatialObject boxHitsOne() {
            return null;
        }

        public DataModel.SpatialObject boxHitsSome() {
            return null;
        }

        public DataModel.SpatialObject boxHitsAll() {
            return null;
        }

        protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsSome() {
            throw new IllegalStateException(format("This method shouldn't be called %s", this));
        }

        protected Iterable<? extends DataModel.SpatialObject> expectedResultForPointHitsOne() {
            throw new IllegalStateException(format("This method shouldn't be called %s", this));
        }

        protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsSome() {
            throw new IllegalStateException(format("This method shouldn't be called %s", this));
        }

        protected Iterable<? extends DataModel.SpatialObject> expectedResultForBoxHitsOne() {
            throw new IllegalStateException(format("This method shouldn't be called %s", this));
        }

    }

    public enum QueryProvider {
        POINT_HITS_NONE {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.pointHitsNone();
            }
        },
        POINT_HITS_ONE {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.pointHitsOne();
            }
        },
        POINT_HITS_SOME {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.pointHitsSome();
            }
        },
        BOX_HITS_NONE {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.boxHitsNone();
            }
        },
        BOX_HITS_ONE {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.boxHitsOne();
            }
        },
        BOX_HITS_SOME {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.boxHitsSome();
            }
        },
        BOX_HITS_ALL {
            @Override
            public DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider) {
                return spatialObjectSetProvider.boxHitsAll();
            }
        },;

        public abstract DataModel.SpatialObject get(SpatialObjectSetProvider spatialObjectSetProvider);
    }

    @FactorField
    public SpaceFactory spaceFactory;

    @FactorField(includeNull = true)
    public SpatialIndex.Options options;

    @FactorField
    public Boolean stableRecords;

    @FactorField
    public SpatialJoin.Duplicates duplicates;

    @FactorField
    public SpatialObjectSetProvider spatialObjectSetProvider;

    @FactorField
    public QueryProvider queryProvider;

    @Condition(constraint = true)
    @Uses({"queryProvider", "spatialObjectSetProvider"})
    public boolean check() {
        return this.queryProvider.get(this.spatialObjectSetProvider) != null;
    }

    protected Space space;

    protected DataModel.Index index;

    protected SpatialIndex<DataModel.Record> spatialIndex;

    protected SpatialJoin session;

    protected SpatialObject query;

    protected Iterable<? extends DataModel.SpatialObject> spatialObjects;

    @Before
    public void wireObjects() throws IOException, InterruptedException {
        boolean stableRecords = this.stableRecords != null ? this.stableRecords : true;
        this.space = this.spaceFactory.create();
        this.index = new DataModel.Index(stableRecords);
        this.spatialIndex = createSpatialIndex(this.space, this.index, SpatialIndex.Options.DEFAULT/*this.options*/);

        this.session = SpatialJoin.newSpatialJoin(this.duplicates, DataModel.SpatialJoinFilter.INSTANCE);
        this.spatialObjects = this.spatialObjectSetProvider.get();

        DataModel.Record.Builder recordBuilder = new DataModel.Record.Builder(stableRecords);
        int i = 0;
        for (DataModel.SpatialObject each : this.spatialObjects) {
            this.spatialIndex.add(each, recordBuilder.with(each));
        }

        this.spatialObjects = this.spatialObjectSetProvider.get();
        this.query = this.queryProvider.get(this.spatialObjectSetProvider);
    }

    @Test
    public void printTestCase() {
        System.out.println(TestCaseUtils.toTestCase(this));
    }

    @Test
    public void printQuery() {
        System.out.println("query=" + this.query);
    }

    @Test
    public void printObjectSet() throws IOException, InterruptedException {
        int count = 0;
        for (DataModel.SpatialObject each : this.spatialObjects) {
            System.out.println(each);
            count++;
        }
        System.out.println(count + " objects are added to SpatialIndex");
    }


    @Test
    public void printQueryResult() throws IOException, InterruptedException {
        int count = 0;
        for (DataModel.SpatialObject each : performQuery(this.spatialIndex, this.session, this.query)) {
            System.out.println(each);
            count++;
        }
        System.out.println(count + " objects hit " + this.query);
    }

    @Test
    public void verifyQueryResult() throws IOException, InterruptedException {
        assertEquals(
                this.spatialObjectSetProvider.expectedResultFor(this.queryProvider),
                performQuery(this.spatialIndex, this.session, this.query));
    }

    private static List<DataModel.SpatialObject> performQuery(
            SpatialIndex<DataModel.Record> spatialIndex,
            SpatialJoin session,
            SpatialObject query) throws IOException, InterruptedException {
        return asList(
                toArray(
                        transform(
                                session.iterator(query, spatialIndex),
                                new Function<DataModel.Record, DataModel.SpatialObject>() {
                                    @Override
                                    public DataModel.SpatialObject apply(DataModel.Record input) {
                                        return (DataModel.SpatialObject) input.spatialObject();
                                    }
                                }),
                        DataModel.SpatialObject.class)
        );
    }

    private static SpatialIndex<DataModel.Record> createSpatialIndex(Space space, DataModel.Index index, SpatialIndex.Options options) throws IOException, InterruptedException {
        if (options == null) {
            return SpatialIndex.newSpatialIndex(space, index);
        } else {
            return SpatialIndex.newSpatialIndex(space, index, options);
        }
    }
}

