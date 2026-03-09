package Thexiaoyu.underworld.util;


public class FastNoise {

    public enum NoiseType {
        Value,
        ValueFractal,
        Perlin,
        PerlinFractal,
        Simplex,
        SimplexFractal,
        Cellular,
        WhiteNoise,
        Cubic,
        CubicFractal
    }

    public enum Interp {
        Linear,
        Hermite,
        Quintic
    }

    public enum FractalType {
        FBM,
        Billow,
        RigidMulti
    }

    public enum CellularDistanceFunction {
        Euclidean,
        Manhattan,
        Natural
    }

    public enum CellularReturnType {
        CellValue,
        Distance,
        Distance2,
        Distance2Add,
        Distance2Sub,
        Distance2Mul,
        Distance2Div
    }

    private int seed = 1337;
    private float frequency = 0.01f;
    private Interp interp = Interp.Quintic;
    private NoiseType noiseType = NoiseType.Simplex;

    private int octaves = 3;
    private float lacunarity = 2.0f;
    private float gain = 0.5f;
    private FractalType fractalType = FractalType.FBM;

    private CellularDistanceFunction cellularDistanceFunction = CellularDistanceFunction.Euclidean;
    private CellularReturnType cellularReturnType = CellularReturnType.CellValue;

    private float fractalBounding;

    public FastNoise() {
        this(1337);
    }

    public FastNoise(int seed) {
        this.seed = seed;
        CalculateFractalBounding();
    }

    // Setters
    public void SetSeed(int seed) {
        this.seed = seed;
    }

    public void SetFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void SetInterp(Interp interp) {
        this.interp = interp;
    }

    public void SetNoiseType(NoiseType noiseType) {
        this.noiseType = noiseType;
    }

    public void SetFractalOctaves(int octaves) {
        this.octaves = octaves;
        CalculateFractalBounding();
    }

    public void SetFractalLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public void SetFractalGain(float gain) {
        this.gain = gain;
        CalculateFractalBounding();
    }

    public void SetFractalType(FractalType fractalType) {
        this.fractalType = fractalType;
    }

    public void SetCellularDistanceFunction(CellularDistanceFunction cellularDistanceFunction) {
        this.cellularDistanceFunction = cellularDistanceFunction;
    }

    public void SetCellularReturnType(CellularReturnType cellularReturnType) {
        this.cellularReturnType = cellularReturnType;
    }

    private void CalculateFractalBounding() {
        float amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        fractalBounding = 1 / ampFractal;
    }

    // Hash functions
    private static final int X_PRIME = 1619;
    private static final int Y_PRIME = 31337;
    private static final int Z_PRIME = 6971;
    private static final int W_PRIME = 1013;

    private static int Hash2D(int seed, int x, int y) {
        int hash = seed;
        hash ^= X_PRIME * x;
        hash ^= Y_PRIME * y;

        hash = hash * hash * hash * 60493;
        hash = (hash >> 13) ^ hash;

        return hash;
    }

    private static int Hash3D(int seed, int x, int y, int z) {
        int hash = seed;
        hash ^= X_PRIME * x;
        hash ^= Y_PRIME * y;
        hash ^= Z_PRIME * z;

        hash = hash * hash * hash * 60493;
        hash = (hash >> 13) ^ hash;

        return hash;
    }

    private static float ValCoord2D(int seed, int x, int y) {
        int n = Hash2D(seed, x, y);
        return n * (1.0f / 2147483648.0f);
    }

    private static float ValCoord3D(int seed, int x, int y, int z) {
        int n = Hash3D(seed, x, y, z);
        return n * (1.0f / 2147483648.0f);
    }

    // Gradient
    private static final float[] GRAD_2D = {
            -1, -1, 1, -1, -1, 1, 1, 1,
            0, -1, -1, 0, 0, 1, 1, 0
    };

    private static final float[] GRAD_3D = {
            1, 1, 0, -1, 1, 0, 1, -1, 0, -1, -1, 0,
            1, 0, 1, -1, 0, 1, 1, 0, -1, -1, 0, -1,
            0, 1, 1, 0, -1, 1, 0, 1, -1, 0, -1, -1,
            1, 1, 0, 0, -1, 1, -1, 1, 0, 0, -1, -1
    };

    private static float GradCoord2D(int seed, int x, int y, float xd, float yd) {
        int hash = Hash2D(seed, x, y);
        hash ^= hash >> 15;
        hash &= 7 << 1;

        return xd * GRAD_2D[hash] + yd * GRAD_2D[hash + 1];
    }

    private static float GradCoord3D(int seed, int x, int y, int z, float xd, float yd, float zd) {
        int hash = Hash3D(seed, x, y, z);
        hash ^= hash >> 15;
        hash &= 63;
        hash = (hash % 12) * 3;

        return xd * GRAD_3D[hash] + yd * GRAD_3D[hash + 1] + zd * GRAD_3D[hash + 2];
    }

    // Main noise functions
    public float GetNoise(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (noiseType) {
            case Value:
                return SingleValue(seed, x, y);
            case ValueFractal:
                return SingleValueFractalFBM(x, y);
            case Perlin:
                return SinglePerlin(seed, x, y);
            case PerlinFractal:
                return SinglePerlinFractalFBM(x, y);
            case Simplex:
                return SingleSimplex(seed, x, y);
            case SimplexFractal:
                return SingleSimplexFractalFBM(x, y);
            case Cellular:
                return SingleCellular(x, y);
            case WhiteNoise:
                return GetWhiteNoise(x, y);
            default:
                return 0;
        }
    }

    public float GetNoise(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (noiseType) {
            case Value:
                return SingleValue(seed, x, y, z);
            case ValueFractal:
                return SingleValueFractalFBM(x, y, z);
            case Perlin:
                return SinglePerlin(seed, x, y, z);
            case PerlinFractal:
                return SinglePerlinFractalFBM(x, y, z);
            case Simplex:
                return SingleSimplex(seed, x, y, z);
            case SimplexFractal:
                return SingleSimplexFractalFBM(x, y, z);
            case Cellular:
                return SingleCellular(x, y, z);
            case WhiteNoise:
                return GetWhiteNoise(x, y, z);
            default:
                return 0;
        }
    }

    // White Noise
    public float GetWhiteNoise(float x, float y) {
        int xi = Float.floatToIntBits(x);
        int yi = Float.floatToIntBits(y);
        return ValCoord2D(seed, xi, yi);
    }

    public float GetWhiteNoise(float x, float y, float z) {
        int xi = Float.floatToIntBits(x);
        int yi = Float.floatToIntBits(y);
        int zi = Float.floatToIntBits(z);
        return ValCoord3D(seed, xi, yi, zi);
    }

    // Value Noise 2D
    private float SingleValue(int seed, float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float xs, ys;
        switch (interp) {
            default:
            case Linear:
                xs = x - x0;
                ys = y - y0;
                break;
            case Hermite:
                xs = InterpHermiteFunc(x - x0);
                ys = InterpHermiteFunc(y - y0);
                break;
            case Quintic:
                xs = InterpQuinticFunc(x - x0);
                ys = InterpQuinticFunc(y - y0);
                break;
        }

        float xf0 = lerp(ValCoord2D(seed, x0, y0), ValCoord2D(seed, x1, y0), xs);
        float xf1 = lerp(ValCoord2D(seed, x0, y1), ValCoord2D(seed, x1, y1), xs);

        return lerp(xf0, xf1, ys);
    }

    // Value Noise 3D
    private float SingleValue(int seed, float x, float y, float z) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float xs, ys, zs;
        switch (interp) {
            default:
            case Linear:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                break;
            case Hermite:
                xs = InterpHermiteFunc(x - x0);
                ys = InterpHermiteFunc(y - y0);
                zs = InterpHermiteFunc(z - z0);
                break;
            case Quintic:
                xs = InterpQuinticFunc(x - x0);
                ys = InterpQuinticFunc(y - y0);
                zs = InterpQuinticFunc(z - z0);
                break;
        }

        float xf00 = lerp(ValCoord3D(seed, x0, y0, z0), ValCoord3D(seed, x1, y0, z0), xs);
        float xf10 = lerp(ValCoord3D(seed, x0, y1, z0), ValCoord3D(seed, x1, y1, z0), xs);
        float xf01 = lerp(ValCoord3D(seed, x0, y0, z1), ValCoord3D(seed, x1, y0, z1), xs);
        float xf11 = lerp(ValCoord3D(seed, x0, y1, z1), ValCoord3D(seed, x1, y1, z1), xs);

        float yf0 = lerp(xf00, xf10, ys);
        float yf1 = lerp(xf01, xf11, ys);

        return lerp(yf0, yf1, zs);
    }

    // Value Fractal FBM 2D
    private float SingleValueFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = SingleValue(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += SingleValue(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    // Value Fractal FBM 3D
    private float SingleValueFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = SingleValue(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += SingleValue(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    // Perlin Noise 2D
    private float SinglePerlin(int seed, float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float xs, ys;
        switch (interp) {
            default:
            case Linear:
                xs = x - x0;
                ys = y - y0;
                break;
            case Hermite:
                xs = InterpHermiteFunc(x - x0);
                ys = InterpHermiteFunc(y - y0);
                break;
            case Quintic:
                xs = InterpQuinticFunc(x - x0);
                ys = InterpQuinticFunc(y - y0);
                break;
        }

        float xd0 = x - x0;
        float yd0 = y - y0;
        float xd1 = xd0 - 1;
        float yd1 = yd0 - 1;

        float xf0 = lerp(GradCoord2D(seed, x0, y0, xd0, yd0), GradCoord2D(seed, x1, y0, xd1, yd0), xs);
        float xf1 = lerp(GradCoord2D(seed, x0, y1, xd0, yd1), GradCoord2D(seed, x1, y1, xd1, yd1), xs);

        return lerp(xf0, xf1, ys);
    }

    // Perlin Noise 3D
    private float SinglePerlin(int seed, float x, float y, float z) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float xs, ys, zs;
        switch (interp) {
            default:
            case Linear:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                break;
            case Hermite:
                xs = InterpHermiteFunc(x - x0);
                ys = InterpHermiteFunc(y - y0);
                zs = InterpHermiteFunc(z - z0);
                break;
            case Quintic:
                xs = InterpQuinticFunc(x - x0);
                ys = InterpQuinticFunc(y - y0);
                zs = InterpQuinticFunc(z - z0);
                break;
        }

        float xd0 = x - x0;
        float yd0 = y - y0;
        float zd0 = z - z0;
        float xd1 = xd0 - 1;
        float yd1 = yd0 - 1;
        float zd1 = zd0 - 1;

        float xf00 = lerp(GradCoord3D(seed, x0, y0, z0, xd0, yd0, zd0), GradCoord3D(seed, x1, y0, z0, xd1, yd0, zd0), xs);
        float xf10 = lerp(GradCoord3D(seed, x0, y1, z0, xd0, yd1, zd0), GradCoord3D(seed, x1, y1, z0, xd1, yd1, zd0), xs);
        float xf01 = lerp(GradCoord3D(seed, x0, y0, z1, xd0, yd0, zd1), GradCoord3D(seed, x1, y0, z1, xd1, yd0, zd1), xs);
        float xf11 = lerp(GradCoord3D(seed, x0, y1, z1, xd0, yd1, zd1), GradCoord3D(seed, x1, y1, z1, xd1, yd1, zd1), xs);

        float yf0 = lerp(xf00, xf10, ys);
        float yf1 = lerp(xf01, xf11, ys);

        return lerp(yf0, yf1, zs);
    }

    // Perlin Fractal FBM 2D
    private float SinglePerlinFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = SinglePerlin(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += SinglePerlin(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    // Perlin Fractal FBM 3D
    private float SinglePerlinFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = SinglePerlin(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += SinglePerlin(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    // Simplex Noise Constants
    private static final float F2 = 0.36602540378443864676372317075294f;
    private static final float G2 = 0.21132486540518711774542560974902f;
    private static final float F3 = 1.0f / 3.0f;
    private static final float G3 = 1.0f / 6.0f;

    // Simplex Noise 2D
    private float SingleSimplex(int seed, float x, float y) {
        float t = (x + y) * F2;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);

        t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;

        float x0 = x - X0;
        float y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1 + 2 * G2;
        float y2 = y0 - 1 + 2 * G2;

        float n0, n1, n2;

        t = 0.5f - x0 * x0 - y0 * y0;
        if (t < 0)
            n0 = 0;
        else {
            t *= t;
            n0 = t * t * GradCoord2D(seed, i, j, x0, y0);
        }

        t = 0.5f - x1 * x1 - y1 * y1;
        if (t < 0)
            n1 = 0;
        else {
            t *= t;
            n1 = t * t * GradCoord2D(seed, i + i1, j + j1, x1, y1);
        }

        t = 0.5f - x2 * x2 - y2 * y2;
        if (t < 0)
            n2 = 0;
        else {
            t *= t;
            n2 = t * t * GradCoord2D(seed, i + 1, j + 1, x2, y2);
        }

        return 50 * (n0 + n1 + n2);
    }

    // Simplex Noise 3D
    private float SingleSimplex(int seed, float x, float y, float z) {
        float t = (x + y + z) * F3;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);

        t = (i + j + k) * G3;
        float x0 = x - (i - t);
        float y0 = y - (j - t);
        float z0 = z - (k - t);

        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1;
            } else {
                i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1;
            } else if (x0 < z0) {
                i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1;
            } else {
                i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            }
        }

        float x1 = x0 - i1 + G3;
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;
        float x2 = x0 - i2 + 2 * G3;
        float y2 = y0 - j2 + 2 * G3;
        float z2 = z0 - k2 + 2 * G3;
        float x3 = x0 - 1 + 3 * G3;
        float y3 = y0 - 1 + 3 * G3;
        float z3 = z0 - 1 + 3 * G3;

        float n0, n1, n2, n3;

        t = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t < 0)
            n0 = 0;
        else {
            t *= t;
            n0 = t * t * GradCoord3D(seed, i, j, k, x0, y0, z0);
        }

        t = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t < 0)
            n1 = 0;
        else {
            t *= t;
            n1 = t * t * GradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
        }

        t = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t < 0)
            n2 = 0;
        else {
            t *= t;
            n2 = t * t * GradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
        }

        t = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t < 0)
            n3 = 0;
        else {
            t *= t;
            n3 = t * t * GradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
        }

        return 32 * (n0 + n1 + n2 + n3);
    }

    // Simplex Fractal FBM 2D
    private float SingleSimplexFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = SingleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += SingleSimplex(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    // Simplex Fractal FBM 3D
    private float SingleSimplexFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = SingleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += SingleSimplex(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    // Cellular Noise 2D
    private float SingleCellular(float x, float y) {
        int xr = fastRound(x);
        int yr = fastRound(y);

        float distance = 999999;
        float distance2 = 999999;
        int xc = 0, yc = 0;

        switch (cellularDistanceFunction) {
            case Euclidean:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = Hash2D(seed, xi, yi);
                        float vecX = xi - x + (hash & 0xFF) * (1.0f / 255.0f) - 0.5f;
                        float vecY = yi - y + ((hash >> 8) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                        float newDistance = vecX * vecX + vecY * vecY;
                        if (newDistance < distance) {
                            distance2 = distance;
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        } else if (newDistance < distance2) {
                            distance2 = newDistance;
                        }
                    }
                }
                break;
            case Manhattan:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = Hash2D(seed, xi, yi);
                        float vecX = xi - x + (hash & 0xFF) * (1.0f / 255.0f) - 0.5f;
                        float vecY = yi - y + ((hash >> 8) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                        float newDistance = Math.abs(vecX) + Math.abs(vecY);
                        if (newDistance < distance) {
                            distance2 = distance;
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        } else if (newDistance < distance2) {
                            distance2 = newDistance;
                        }
                    }
                }
                break;
            case Natural:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = Hash2D(seed, xi, yi);
                        float vecX = xi - x + (hash & 0xFF) * (1.0f / 255.0f) - 0.5f;
                        float vecY = yi - y + ((hash >> 8) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                        float newDistance = (Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);
                        if (newDistance < distance) {
                            distance2 = distance;
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        } else if (newDistance < distance2) {
                            distance2 = newDistance;
                        }
                    }
                }
                break;
        }

        switch (cellularReturnType) {
            case CellValue:
                return ValCoord2D(seed, xc, yc);
            case Distance:
                return distance;
            case Distance2:
                return distance2;
            case Distance2Add:
                return distance2 + distance;
            case Distance2Sub:
                return distance2 - distance;
            case Distance2Mul:
                return distance2 * distance;
            case Distance2Div:
                return distance / distance2;
            default:
                return 0;
        }
    }

    // Cellular Noise 3D
    private float SingleCellular(float x, float y, float z) {
        int xr = fastRound(x);
        int yr = fastRound(y);
        int zr = fastRound(z);

        float distance = 999999;
        float distance2 = 999999;
        int xc = 0, yc = 0, zc = 0;

        switch (cellularDistanceFunction) {
            case Euclidean:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int hash = Hash3D(seed, xi, yi, zi);
                            float vecX = xi - x + (hash & 0xFF) * (1.0f / 255.0f) - 0.5f;
                            float vecY = yi - y + ((hash >> 8) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                            float vecZ = zi - z + ((hash >> 16) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                            float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;
                            if (newDistance < distance) {
                                distance2 = distance;
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            } else if (newDistance < distance2) {
                                distance2 = newDistance;
                            }
                        }
                    }
                }
                break;
            default:
                // Manhattan and Natural similar implementation
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int hash = Hash3D(seed, xi, yi, zi);
                            float vecX = xi - x + (hash & 0xFF) * (1.0f / 255.0f) - 0.5f;
                            float vecY = yi - y + ((hash >> 8) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                            float vecZ = zi - z + ((hash >> 16) & 0xFF) * (1.0f / 255.0f) - 0.5f;
                            float newDistance = Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ);
                            if (newDistance < distance) {
                                distance2 = distance;
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            } else if (newDistance < distance2) {
                                distance2 = newDistance;
                            }
                        }
                    }
                }
                break;
        }

        switch (cellularReturnType) {
            case CellValue:
                return ValCoord3D(seed, xc, yc, zc);
            case Distance:
                return distance;
            case Distance2:
                return distance2;
            case Distance2Add:
                return distance2 + distance;
            case Distance2Sub:
                return distance2 - distance;
            case Distance2Mul:
                return distance2 * distance;
            case Distance2Div:
                return distance / distance2;
            default:
                return 0;
        }
    }

    // Utility functions
    private static int fastFloor(float f) {
        return (f >= 0 ? (int) f : (int) f - 1);
    }

    private static int fastRound(float f) {
        return (f >= 0) ? (int) (f + 0.5f) : (int) (f - 0.5f);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static float InterpHermiteFunc(float t) {
        return t * t * (3 - 2 * t);
    }

    private static float InterpQuinticFunc(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
}