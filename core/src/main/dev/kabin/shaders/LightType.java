package dev.kabin.shaders;


public enum LightType {

    CONE {

        final static int VALUE = 10;
        final static float VALUE_AS_FLOAT = Float.intBitsToFloat(VALUE);

        @Override
        public int getValue() {
            return VALUE;
        }

        @Override
        public float getFloatValue() {
            return VALUE_AS_FLOAT;
        }
    },

    SPHERE {

        final static int VALUE = 20;

        final static float VALUE_AS_FLOAT = Float.intBitsToFloat(VALUE);

        @Override
        public int getValue() {
            return VALUE;
        }

        @Override
        public float getFloatValue() {
            return VALUE_AS_FLOAT;
        }

    },

    BEAM {

        final static int VALUE = 30;

        final static float VALUE_AS_FLOAT = Float.intBitsToFloat(VALUE);

        @Override
        public int getValue() {
            return VALUE;
        }

        @Override
        public float getFloatValue() {
            return VALUE_AS_FLOAT;
        }

    };

    public abstract int getValue();

    public abstract float getFloatValue();
}
