package io.github.slimeistdev.volare.content.glider;

import io.github.slimeistdev.volare.Volare;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

// http://airfoiltools.com/polar/details?polar=xf-n0012-il-1000000
public class Airfoil {
	private static final float C_P = 1.9f; // Coefficient of pressure for flat plate
	private static final float EXTRAPOLATION_DIST = 6.5f;
	private static final float BLEND_DIST = 35f;

	private final float alphaMin;
	private final float alphaMax;
	private final float alphaStep;

	private final float[] cl;
	private final float[] cd;

	public final float clMax;
	private final float clMin;

	private final float cdMax;

	private final float minBlendStart;
	private final float minBlendEnd;
	private final float maxBlendStart;
	private final float maxBlendEnd;

	public Airfoil(float alphaMin, float alphaMax, float alphaStep, float[] cl, float[] cd) {
		assert cl.length == cd.length : "Lift and drag coefficients must have the same length";
		int expectedSteps = (int) ((alphaMax - alphaMin) / alphaStep) + 1;
		assert cl.length == expectedSteps : "Lift and drag coefficients must have length " + expectedSteps + ", got " + cl.length;

		this.alphaMin = alphaMin;
		this.alphaMax = alphaMax;
		this.alphaStep = alphaStep;

		this.cl = cl;
		this.cd = cd;

		float clMin = Float.POSITIVE_INFINITY;
		float clMax = Float.NEGATIVE_INFINITY;
		for (float value : cl) {
			if (value < clMin) {
				clMin = value;
			}
			if (value > clMax) {
				clMax = value;
			}
		}
		this.clMin = clMin;
		this.clMax = clMax;

		float cdMax = Float.NEGATIVE_INFINITY;
		for (float value : cd) {
			if (value > cdMax) {
				cdMax = value;
			}
		}
		this.cdMax = cdMax;

		this.minBlendStart = MathHelper.abs(alphaMin) + EXTRAPOLATION_DIST;
		this.minBlendEnd = this.minBlendStart + BLEND_DIST;
		this.maxBlendStart = MathHelper.abs(alphaMax) + EXTRAPOLATION_DIST;
		this.maxBlendEnd = this.maxBlendStart + BLEND_DIST;
	}

	private static float getFlatPlateCl(float alpha) {
		return (C_P / 2) * MathHelper.sin(2 * alpha * RADIANS_PER_DEGREE);
	}

	private static float getFlatPlateCd(float alpha) {
		float sinAlpha = MathHelper.sin(alpha * RADIANS_PER_DEGREE);
		return C_P * sinAlpha * sinAlpha;
	}

	private float blendToFlat(float alpha, float c_foil, float c_flat) {
		float blendStart = alpha < 0 ? minBlendStart : maxBlendStart;
		float blendEnd = alpha < 0 ? minBlendEnd : maxBlendEnd;

		float absAlpha = MathHelper.abs(alpha);

		if (absAlpha <= blendStart) {
			return c_foil;
		} else if (absAlpha >= blendEnd) {
			return c_flat;
		} else {
			float factor = (absAlpha - blendStart) / BLEND_DIST;
			factor = factor * factor * (3 - 2 * factor);
			return MathHelper.lerp(factor, c_foil, c_flat);
		}
	}

	private float signLockedExtrapolate(float penultimateValue, float ultimateValue, float excessAlpha) {
		float slope = (ultimateValue - penultimateValue) / alphaStep;
		float extrapolatedValue = ultimateValue + slope * Math.abs(excessAlpha);

		if (ultimateValue < 0) {
			return Math.min(extrapolatedValue, 0);
		} else {
			return Math.max(0, extrapolatedValue);
		}
	}

	private float getValue(float alpha, float[] values) {
		if (alpha < -90 || alpha > 90) {
			Volare.LOG.error("Airfoil coefficient calculation error: Alpha must be in the range [-90, 90], got {}", alpha);
		}

		if (alpha < alphaMin) {
			float excessAlpha = alphaMin - alpha;
			return signLockedExtrapolate(values[1], values[0], excessAlpha);
		} else if (alpha > alphaMax) {
			float excessAlpha = alpha - alphaMax;
			return signLockedExtrapolate(values[values.length - 2], values[values.length - 1], excessAlpha);
		} else {
			int lowerIndex = (int) ((alpha - alphaMin) / alphaStep);
			int upperIndex = lowerIndex + 1;

			float factor = (alpha - (alphaMin + lowerIndex * alphaStep)) / alphaStep;
			if (upperIndex >= values.length) {
				return values[lowerIndex];
			}

			return MathHelper.lerp(factor, values[lowerIndex], values[upperIndex]);
		}
	}

	public float getCl(float alpha) {
		float value = blendToFlat(alpha, getValue(alpha, cl), getFlatPlateCl(alpha));
		return MathHelper.clamp(value, clMin * 1.5f, clMax * 1.5f);
	}

	public float getCd(float alpha) {
		float value = blendToFlat(alpha, getValue(alpha, cd), getFlatPlateCd(alpha));
		return MathHelper.clamp(value, 1e-6f, cdMax * 25.0f);
	}
}
