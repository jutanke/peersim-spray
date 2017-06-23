package descent.observers;

import java.util.Collections;
import java.util.List;

public class Stats {

	public final Double mean;
	public final Double median;
	public final Double min;
	public final Double max;
	public final Double stdDev;

	public Stats(Double mean, Double median, Double min, Double max, Double stdDev) {
		this.mean = mean;
		this.median = median;
		this.min = min;
		this.max = max;
		this.stdDev = stdDev;
	}

	@Override
	public String toString() {
		return this.mean + " " + this.median + " " + this.min + " " + this.max + " " + this.stdDev;
	}

	/**
	 * Get stats from a list containing large values
	 * 
	 * @param values
	 *            the list of values to aggregate
	 * @return Stats on that list
	 */
	public static Stats getFromLarge(List<Double> values) {
		Collections.sort(values);
		Double mean = 0.;
		Double max = Double.NEGATIVE_INFINITY;
		Double min = Double.POSITIVE_INFINITY;
		Double med = 0.;
		Double stdDev = 0.;

		Integer num = 0;
		for (Double value : values) {
			// #A median
			if (values.size() % 2 == 1 && num == values.size() / 2) {
				med = value;
			} else if (values.size() % 2 == 0 && (num == values.size() / 2 - 1 || num == values.size() / 2)) {
				med += value;
			}
			++num;
			// #B average
			mean += (value / values.size());

			// #C max
			if (max < value) {
				max = value;
			}
			// #D min
			if (min > value) {
				min = value;
			}
		}

		if (values.size() % 2 == 0) {
			med = med / 2.;
		}

		// #E standard deviation
		Double var = 0.;
		for (Double value : values) {
			Double c = value - mean;
			var += c * c / values.size();
		}
		stdDev = Math.sqrt(var);

		return new Stats(mean, med, min, max, stdDev);
	}

	/**
	 * Get stats from a list containing small values
	 * 
	 * @param values
	 *            the list of values to aggregate
	 * @return Stats on that list
	 */
	public static Stats getFromSmall(List<Double> values) {
		Collections.sort(values);
		Double mean = 0.;
		Double max = Double.NEGATIVE_INFINITY;
		Double min = Double.POSITIVE_INFINITY;
		Double med = 0.;
		Double stdDev = 0.;

		Integer num = 0;
		for (Double value : values) {
			// #A median
			if (values.size() % 2 == 1 && num == values.size() / 2) {
				med = value;
			} else if (values.size() % 2 == 0 && (num == values.size() / 2 - 1 || num == values.size() / 2)) {
				med += value;
			}
			++num;
			// #B average
			mean += value;

			// #C max
			if (max < value) {
				max = value;
			}
			// #D min
			if (min > value) {
				min = value;
			}
		}
		mean = mean / values.size();

		if (values.size() % 2 == 0) {
			med = med / 2.;
		}

		// #E standard deviation
		Double var = 0.;
		for (Double value : values) {
			Double c = value - mean;
			var += c * c;
		}
		var = var / values.size();
		stdDev = Math.sqrt(var);

		return new Stats(mean, med, min, max, stdDev);
	}

}
