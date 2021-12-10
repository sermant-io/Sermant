/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.bootstrap.utils;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 浮点数的原子类，底层通过long值来实现 <br>
 *
 * @author
 * @since 2020年3月9日
 */
public class AtomicDouble extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    private static final AtomicLongFieldUpdater<AtomicDouble> UPDATER = AtomicLongFieldUpdater.newUpdater(
        AtomicDouble.class, "value");

    private transient volatile long value;

    public AtomicDouble(double initialValue) {
        value = doubleToRawLongBits(initialValue);
    }

    public AtomicDouble() {
        // assert doubleToRawLongBits(0.0) == 0L;
    }

    public final double get() {
        return longBitsToDouble(value);
    }

    public final void set(double newValue) {
        long next = doubleToRawLongBits(newValue);
        value = next;
    }

    public final void lazySet(double newValue) {
        set(newValue);
        // TODO(user): replace with code below when jdk5 support is dropped.
    }

    public final double getAndSet(double newValue) {
        long next = doubleToRawLongBits(newValue);
        return longBitsToDouble(UPDATER.getAndSet(this, next));
    }

    public final boolean compareAndSet(double expect, double update) {
        return UPDATER.compareAndSet(this, doubleToRawLongBits(expect), doubleToRawLongBits(update));
    }

    public final boolean weakCompareAndSet(double expect, double update) {
        return UPDATER.weakCompareAndSet(this, doubleToRawLongBits(expect), doubleToRawLongBits(update));
    }

    public final double getAndAdd(double delta) {
        while (true) {
            long current = value;
            double currentVal = longBitsToDouble(current);
            double nextVal = currentVal + delta;
            long next = doubleToRawLongBits(nextVal);
            if (UPDATER.compareAndSet(this, current, next)) {
                return currentVal;
            }
        }
    }

    public final double addAndGet(double delta) {
        while (true) {
            long current = value;
            double currentVal = longBitsToDouble(current);
            double nextVal = currentVal + delta;
            long next = doubleToRawLongBits(nextVal);
            if (UPDATER.compareAndSet(this, current, next)) {
                return nextVal;
            }
        }
    }

    public String toString() {
        return Double.toString(get());
    }

    public int intValue() {
        return (int) get();
    }

    public long longValue() {
        return (long) get();
    }

    public float floatValue() {
        return (float) get();
    }

    public double doubleValue() {
        return get();
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();

        s.writeDouble(get());
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        set(s.readDouble());
    }
}
