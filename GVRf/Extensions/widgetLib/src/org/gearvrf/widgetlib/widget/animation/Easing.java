package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.log.Log;

import org.gearvrf.animation.GVRInterpolator;

public enum Easing implements GVRInterpolator {
    BACK_IN {
        @Override
        public float mapRatio(float ratio) {
            float v = ratio * ratio * ((sBackFactor + 1) * ratio - sBackFactor);
            Log.d(TAG, "%s.mapRatio(): in: %.2f, out: %.2f", this, ratio, v);
            return v;
        }
    },
    BACK_OUT {
        @Override
        public float mapRatio(float ratio) {
            ratio = ratio - 1;
            return ratio * ratio * ((sBackFactor + 1) * ratio + sBackFactor) + 1;
        }
    },
    BACK_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            ratio = ratio * 2;
            if (ratio < 1) {
                return helper(ratio, -1, 0);
            } else {
                ratio = ratio - 2;
                return helper(ratio, 1, 2);
            }
        }

        private float helper(float ratio, int factor, int inOutOffset) {
            return (float) (0.5 *
                    (ratio * ratio *
                            ((sBackFactor * 1.525 + 1) * ratio + (sBackFactor * factor) * 1.525) +
                            inOutOffset));
        }
    },
    BOUNCE_IN {
        @Override
        public float mapRatio(float ratio) {
            return 1 - BOUNCE_OUT.mapRatio(1 - ratio);
        }
    },
    BOUNCE_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio < 1 / 2.75f) {
                return calcBase(ratio);
            } else if (ratio < 2 / 2.75f) {
                ratio = ratio - 1.5f / 2.75f;
                return calcBase(ratio) + 0.75f;
            } else if (ratio < 2.5f / 2.75f) {
                ratio = ratio - 2.25f / 2.75f;
                return calcBase(ratio) + 0.9375f;
            } else {
                ratio = ratio - 2.625f / 2.75f;
                return calcBase(ratio) + 0.984375f;
            }
        }

        private float calcBase(float ratio) {
            return 7.5625f * ratio * ratio;
        }
    },
    BOUNCE_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            ratio = ratio * 2;
            if (ratio < 1) {
                return 0.5f * BOUNCE_IN.mapRatio(ratio);
            } else {
                return 0.5f * BOUNCE_OUT.mapRatio(ratio - 1) + 0.5f;
            }
        }
    },
    CIRCULAR_IN {
        @Override
        public float mapRatio(float ratio) {
            return (float) -(Math.sqrt(1 - ratio * ratio) - 1);
        }
    },
    CIRCULAR_OUT {
        @Override
        public float mapRatio(float ratio) {
            return (float) Math.sqrt(1 - (ratio - 1) * (ratio - 1));
        }
    },
    CIRCULAR_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            ratio = ratio * 2;
            if (ratio < 1) {
                return (float) (-0.5f * (Math.sqrt(1 - ratio * ratio) - 1));
            } else {
                ratio = ratio - 2;
                return (float) (0.5f * (Math.sqrt(1 - ratio * ratio) + 1));
            }
        }
    },
    CUBIC_IN {
        @Override
        public float mapRatio(float ratio) {
            return ratio * ratio * ratio;
        }
    },
    CUBIC_OUT {
        @Override
        public float mapRatio(float ratio) {
            return 1 - CUBIC_IN.mapRatio(1 - ratio);
        }
    },
    CUBIC_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio < 0.5) {
                return CUBIC_IN.mapRatio(ratio * 2.0f) / 2.0f;
            }

            return 1 - CUBIC_IN.mapRatio((1 - ratio) * 2.0f) / 2.0f;
        }
    },
    ELASTIC_IN {
        @Override
        public float mapRatio(float ratio) {
            if (ratio == 0 || ratio == 1) {
                return ratio;
            }
            ratio = ratio - 1;
            return (float) -(sElasticA *
                    Math.pow(2, 10 * ratio) *
                    Math.sin((ratio - sElasticS) * (2 * Math.PI) / sElasticP));
        }
    },
    ELASTIC_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio == 0 || ratio == 1) {
                return ratio;
            }
            return (float) (sElasticA *
                    Math.pow(2, -10 * ratio) *
                    Math.sin((ratio - sElasticS) * (2 * Math.PI) / sElasticP) + 1);
        }
    },
    ELASTIC_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio == 0 || ratio == 1) {
                return ratio;
            }
            ratio = ratio * 2 - 1;
            if (ratio < 0) {
                return (float) (-0.5 * (sElasticA *
                        Math.pow(2, 10 * ratio) *
                        Math.sin((ratio - sElasticS * 1.5) * (2 * Math.PI) / (sElasticP * 1.5))));
            }
            return (float) (0.5 * sElasticA *
                    Math.pow(2, -10 * ratio) *
                    Math.sin((ratio - sElasticS * 1.5) * (2 * Math.PI) / (sElasticP * 1.5)) + 1);
        }
    },
    EXPONENTIAL_IN {
        @Override
        public float mapRatio(float ratio) {
            if (ratio == 0) {
                return 0;
            }
            return (float) Math.pow(2, 10 * (ratio - 1));
        }
    },
    EXPONENTIAL_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio == 1) {
                return 1;
            }
            return (float) (1 - Math.pow(2, -10 * ratio));
        }
    },
    EXPONENTIAL_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio == 0 | ratio == 1) {
                return ratio;
            }
            ratio = ratio * 2 - 1;
            if (0 > ratio) {
                return (float) (0.5 * Math.pow(2, 10 * ratio));
            }
            return (float) (1 - 0.5 * Math.pow(2, -10 * ratio));
        }
    },
    LINEAR {
        @Override
        public float mapRatio(float ratio) {
            return ratio;
        }
    },
    QUAD_IN {
        @Override
        public float mapRatio(float ratio) {
            return ratio * ratio;
        }
    },
    QUAD_OUT {
        @Override
        public float mapRatio(float ratio) {
            return 1 - QUAD_IN.mapRatio(1 - ratio);
        }
    },
    QUAD_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio < 0.5) {
                return 2 * ratio * ratio;
            }
            return -2 * ratio * (ratio - 2) - 1;
        }
    },
    QUART_IN {
        @Override
        public float mapRatio(float ratio) {
            return ratio * ratio * ratio * ratio;
        }
    },
    QUART_OUT {
        @Override
        public float mapRatio(float ratio) {
            ratio = ratio - 1;
            return 1 - ratio * ratio * ratio * ratio;
        }
    },
    QUART_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio < 0.5) {
                return 8 * ratio * ratio * ratio * ratio;
            }

            ratio = ratio - 1;
            return -8 * ratio * ratio * ratio * ratio + 1;
        }
    },
    QUINT_IN {
        @Override
        public float mapRatio(float ratio) {
            return ratio * ratio * ratio * ratio * ratio;
        }
    },
    QUINT_OUT {
        @Override
        public float mapRatio(float ratio) {
            ratio = ratio - 1;
            return 1 + ratio * ratio * ratio * ratio * ratio;
        }
    },
    QUINT_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            if (ratio < 0.50) {
                return 16 * ratio * ratio * ratio * ratio * ratio;
            }
            ratio = ratio - 1;
            return 16 * ratio * ratio * ratio * ratio * ratio + 1;
        }
    },
    SINE_IN {
        @Override
        public float mapRatio(float ratio) {
            return (float) (1 - Math.cos(ratio * (Math.PI / 2.0f)));
        }
    },
    SINE_OUT {
        @Override
        public float mapRatio(float ratio) {
            return (float) Math.sin(ratio * (Math.PI / 2.0f));
        }
    },
    SINE_IN_OUT {
        @Override
        public float mapRatio(float ratio) {
            return (float) (-0.5 * (Math.cos(ratio * Math.PI) - 1));
        }
    };

    public float mapRatio(float ratio) {
        Log.d(TAG, "mapRatio(): default called");
        return 0;
    }

    private static float sBackFactor = 1.70158f;
    private static float sElasticA = 1;
    private static float sElasticP = 0.3f;
    private static float sElasticS = sElasticP / 4;

    private static final String TAG = Easing.class.getSimpleName();
}
