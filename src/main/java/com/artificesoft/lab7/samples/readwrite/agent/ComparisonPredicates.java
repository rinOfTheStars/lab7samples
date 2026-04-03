package com.artificesoft.lab7.samples.readwrite.agent;

import com.artificesoft.lab7.samples.readwrite.ComparisonPredicate;
import com.artificesoft.lab7.samples.readwrite.ReadWriteSim;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatSnapshot;

public final class ComparisonPredicates {

    public static final ComparisonPredicate<SeatSnapshot> FRUGAL = (e1, e2) -> {
        if (e1.cost() < e2.cost() && !e1.isPurchased()) {
            return e1;
        } else if (e1.cost() > e2.cost() && !e2.isPurchased()) {
            return e2;
        } else if (e1.isPurchased() && !e2.isPurchased()) {
            return e2;
        } else if (e2.isPurchased() && !e1.isPurchased()) {
            return e1;
        } else return e1;
    };

    public static final ComparisonPredicate<SeatSnapshot> SUPERFAN = (e1, e2) -> {
        if (!e1.isPurchased() && !e2.isPurchased()) {
            return FRUGAL.compare(e1, e2);
        } else if (!e1.isPurchased()) return e1;
        else return e2;
    };

    public static final ComparisonPredicate<SeatSnapshot> NO_FRONT_ROWS = (e1, e2) -> {
        if (e1.row() != 0 && e2.row() != 0) return e1;
        else if (e2.row() == 0 && e1.row() != 0) return e1;
        else if (e2.row() != 0) return e2;
        else return e1;
    };

    public static final ComparisonPredicate<SeatSnapshot> NO_BACK_ROWS = (e1, e2) -> {
        int lastRow = ReadWriteSim.ROWS - 1;
        if (e1.row() != lastRow && e2.row() != lastRow) return e1;
        else if (e2.row() == lastRow && e1.row() != lastRow) return e1;
        else if (e2.row() != lastRow) return e2;
        else return e1;
    };

    public static ComparisonPredicate<SeatSnapshot> and(ComparisonPredicate<SeatSnapshot> a, ComparisonPredicate<SeatSnapshot> b) {
        return (e1, e2) -> {
            SeatSnapshot first = a.compare(e1, e2);
            SeatSnapshot second = b.compare(e1, e2);
            if (first.equals(second)) return first;
            else return e1;
        };
    }

    public static ComparisonPredicate<SeatSnapshot> or(ComparisonPredicate<SeatSnapshot> a, ComparisonPredicate<SeatSnapshot> b) {
        return (e1, e2) -> {
            SeatSnapshot first = a.compare(e1, e2);
            SeatSnapshot second = b.compare(e1, e2);
            if (first.equals(second)) return first;
            else if (e2.equals(first)) return e2;
            else if (e2.equals(second)) return e2;
            else return e1;
        };
    }
}
