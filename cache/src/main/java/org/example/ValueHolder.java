package org.example;

import javax.annotation.Nullable;

public class ValueHolder<N> {
    public final N value;

    public ValueHolder(@Nullable N value) {
        this.value = value;
    }
}
