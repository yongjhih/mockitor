/*
 * Copyright (C) 2016 8tory, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mocker;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class Mocker<T> {
    public final Class<T> clazz;
    public Func1<T, ?> when;
    public Func1<T, ?> thenReturn;
    public Func2<T, Integer, ?> when2;
    public Func2<T, Integer, ?> thenReturn2;
    public Action1<T> verify;
    public Action2<T, Integer> verify2;
    public Action1<T> then;
    public Action2<T, Integer> then2;
    public Mocker<T> mocker;
    public T that;
    public VerificationMode verification;

    public interface Func0<R> {
        public R call();
    }

    public interface Func1<V, R> {
        public R call(V v);
    }

    public interface Func2<V, V2, R> {
        public R call(V v, V2 v2);
    }

    public interface Action1<V> {
        public void call(V v);
    }

    public interface Action2<V, V2> {
        public void call(V v, V2 v2);
    }

    public Mocker(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Mocker(Mocker<T> mocker) {
        this.clazz = mocker.clazz;
        this.mocker = mocker;
    }

    public <R> Mocker<T> when(Func1<T, R> when) {
        if (this.when != null || this.when2 != null) {
            Mocker<T> mocker = new Mocker<>(this);
            mocker.when(when);
            return mocker;
        }
        this.when = when;
        return this;
    }

    public <R> Mocker<T> when(Func2<T, Integer, R> when) {
        if (this.when != null || this.when2 != null) {
            Mocker<T> mocker = new Mocker<>(this);
            mocker.when(when);
            return mocker;
        }
        this.when2 = when;
        return this;
    }

    public <R> Mocker<T> thenReturn(Func1<T, R> thenReturn) {
        if (when == null && when2 == null) throw new IllegalStateException("Missing when()");
        this.thenReturn = thenReturn;
        return this;
    }

    public <R> Mocker<T> thenReturn(Func2<T, Integer, R> thenReturn) {
        if (when == null && when2 == null) throw new IllegalStateException("Missing when()");
        this.thenReturn2 = thenReturn;
        return this;
    }

    public static <V> Mocker<V> of(Class<V> clazz) {
        return new Mocker<>(clazz);
    }

    public T mock() {
        return mock(0);
    }

    public T mock(int i) {
        if (that == null) that = mock(clazz);

        if (mocker != null) {
            mocker.that = that;
            mocker.mock(i);
        }

        if (when != null) {
            if (thenReturn != null) {
                Mockito.when(when.call(that)).thenReturn(thenReturn.call(that));
            } else if (thenReturn2 != null) {
                Mockito.when(when.call(that)).thenReturn(thenReturn2.call(that, i));
            }
        } else if (when2 != null) {
            if (thenReturn != null) {
                Mockito.when(when2.call(that, i)).thenReturn(thenReturn.call(that));
            } else if (thenReturn2 != null) {
                Mockito.when(when2.call(that, i)).thenReturn(thenReturn2.call(that, i));
            }
        }

        if (then != null) {
            then.call(that);
        } else if (then2 != null) {
            then2.call(that, i);
        }

        // clear that after return, to mack sure next mock() will regenerate mock(clazz) that
        T that = this.that;
        this.that = null;

        if (verify != null) {
            verify.call(that);
            /*
            if (verifications != null) {
                for (VerificationMode verification : verifications) {
                }
            }
            */
            if (verification != null) {
                return Mockito.verify(that, verification);
            } else {
                return Mockito.verify(that);
            }
        } else if (verify2 != null) {
            verify2.call(that, i);
            if (verification != null) {
                return Mockito.verify(that, verification);
            } else {
                return Mockito.verify(that);
            }
        }

        return that;
    }

    /**
     * Support infer instead of Mockito.mock()
     */
    @SuppressWarnings("unchecked")
    public static <V> V mock(Class<V> clazz) {
        return (V) Mockito.mock(clazz);
    }

    /**
     * For import
     */
    public static <V> Mocker<V> mocker(Class<V> clazz) {
        return (Mocker<V>) of(clazz);
    }

    public <V> Mocker<T> then(Action1<T> then) {
        if (this.then != null) {
            Mocker<T> mocker = new Mocker<>(this);
            mocker.then(then);
            return mocker;
        }
        this.then = then;
        return this;
    }

    public <V> Mocker<T> then(Action2<T, Integer> then) {
        if (this.then2 != null) {
            Mocker<T> mocker = new Mocker<>(this);
            mocker.then(then);
            return mocker;
        }
        this.then2 = then;
        return this;
    }

    /**
     * Warning: lift().lift() NPE risk
     */
    public <V> Mocker<T> lift() {
        return mocker;
    }

    public <V> Mocker<T> safeLift() {
        if (mocker == null) return this;
        return mocker;
    }

    // Alias switchMap()
    public Mocker<T> lift(Mocker<T> mocker) {
        this.mocker = mocker;
        return new Mocker<>(this);
    }

    /* TODO
    public Set<T> asSet() {
        return asSet();
    }
    */

    public List<T> asList() {
        return asList(1);
    }

    public List<T> asList(int many) {
        int n = many;
        if (n <= 0) n = 1;

        if (n == 1) return Arrays.asList(mock());

        List<T> mocks = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            mocks.add(mock(i));
        }

        return mocks;
    }

    /*
    public List<VerificationMode> verifications() {
        if (verifications == null) {
            verifications = new ArrayList<>();
        }
        return verifications
    }
    */

    public <V> Mocker<T> times(int times) {
        verification = Mockito.times(times);
        return this;
    }

    public <V> Mocker<T> atLeast(int atLeast) {
        verification = Mockito.atLeast(atLeast);
        return this;
    }

    public <V> Mocker<T> atMost(int atMost) {
        verification = Mockito.atMost(atMost);
        return this;
    }

    public <V> Mocker<T> never() {
        verification = Mockito.never();
        return this;
    }

    public <V> Mocker<T> atLeastOnce() {
        verification = Mockito.atLeastOnce();
        return this;
    }

    /*
    public <V> Mocker<T> times(int times) {
        verification = verification != null ? verification.times(times) : Mockito.times(times);
        return this;
    }

    public <V> Mocker<T> atLeast(int atLeast) {
        verification = verification != null ? verification.atLeast(atLeast) : Mockito.atLeast(atLeast);
        return this;
    }

    public <V> Mocker<T> atMost(int atMost) {
        verification = verification != null ? verification.atMost(atMost) : Mockito.atMost(atMost);
        return this;
    }

    public <V> Mocker<T> never() {
        verification = verification != null ? verification.never() : Mockito.never();
        return this;
    }

    public <V> Mocker<T> atLeastOnce() {
        verification = verification != null ? verification.atLeastOnce() : Mockito.atLeastOnce();
        return this;
    }

    public <V> Mocker<T> timeout(int timeout) {
        verification = verification != null ? verification.timeout(timeout) : Mockito.timeout(timeout);
        return this;
    }
    */

    public <V> Mocker<T> verify(Action1<T> verify) {
        this.verify = verify;
        return this;
    }

    public <V> Mocker<T> verify(Action2<T, Integer> verify) {
        this.verify2 = verify;
        return this;
    }

    public T never(Action1<T> verify) {
        T mock = mock();
        verify.call(mock);
        return Mockito.verify(mock, Mockito.never());
    }

    public T times(Action1<T> verify, int i) {
        T mock = mock();
        verify.call(mock);
        return Mockito.verify(mock, Mockito.times(i));
    }

    /*
    public T times(int i, Action1<T> verify) {
        T mock = mock();
        mock = Mockito.verify(mock, Mockito.times(i));
        verify.call(mock);
        return mock;
    }
    */
}
