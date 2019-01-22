package com.aop.filter.aspect;

@FunctionalInterface
public interface MethodInvoker {


    Object invoke() throws MethodInvoker.ThrowableWrapper;


    @SuppressWarnings("serial")
    class ThrowableWrapper extends RuntimeException {

        private final Throwable original;

        public ThrowableWrapper(Throwable original) {
            super(original.getMessage(), original);
            this.original = original;
        }

        public Throwable getOriginal() {
            return this.original;
        }
    }
}
