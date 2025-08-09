import sys
import csv


def parse_airfoil(filename: str) -> tuple[list[float], list[float], list[float]]:
    expected_keys = ["Alpha", "Cl", "Cd", "Cdp", "Cm", "Top_Xtr", "Bot_Xtr"]

    alpha = []
    cl = []
    cd = []

    found = False

    with open(filename, 'r') as f:
        for row in csv.reader(f):
            if row == expected_keys:
                found = True
                continue

            if found:
                alpha.append(row[0])
                cl.append(row[1])
                cd.append(row[2])

    return list(map(float, alpha)), list(map(float, cl)), list(map(float, cd))


def lerp(factor: float, a: float, b: float) -> float:
    return a + factor * (b - a)


def java_float_array_init(values: list) -> str:
    values_str = [f"{v}f" for v in values]
    return f"new float[] {{{', '.join(values_str)}}}"


def clean_up_airfoil(alpha: list[float], cl: list[float], cd: list[float]) -> tuple[float, float, float]:
    alpha_min = alpha[0]
    alpha_max = alpha[-1]

    assert alpha_min < alpha_max, "Alpha values are not in increasing order."
    assert alpha_min == min(alpha), "Alpha values are not in increasing order."
    assert alpha_max == max(alpha), "Alpha values are not in increasing order."

    interpolate_indices = []

    expected_step = alpha[1] - alpha[0]
    for i in range(1, len(alpha)):
        step = alpha[i] - alpha[i - 1]
        if abs(step - expected_step) >= 1e-6:
            step_factor = step / expected_step
            if abs(round(step_factor) - step_factor) < 1e-6:
                print(
                    f"[{i}] Warning: alpha values are not evenly spaced: {step} != {expected_step}. Will auto-interpolate.",
                    file=sys.stderr)
                interpolate_indices.append((i, int(step_factor)))
            else:
                raise ValueError(
                    f"[{i}] Alpha values are not evenly spaced: {step} != {expected_step}. Cannot auto-interpolate.")

    for interpolation_data in reversed(interpolate_indices):
        i, step_factor = interpolation_data

        prev_cl = cl[i - 1]
        prev_cd = cd[i - 1]
        next_cl = cl[i]
        next_cd = cd[i]

        for j in range(step_factor - 1, 0, -1):
            cl_v = lerp(prev_cl, next_cl, j / step_factor)
            cd_v = lerp(prev_cd, next_cd, j / step_factor)

            cl.insert(i, cl_v)
            cd.insert(i, cd_v)

    return alpha_min, alpha_max, expected_step


def main() -> None:
    if len(sys.argv) == 2:
        filename = sys.argv[1]
    else:
        filename = input("Filename: ")

    alpha: list[float]
    cl: list[float]
    cd: list[float]
    alpha, cl, cd = parse_airfoil(filename)

    alpha_min: float
    alpha_max: float
    expected_step: float
    alpha_min, alpha_max, expected_step = clean_up_airfoil(alpha, cl, cd)

    print(f"cd max = {max(cd)}", file=sys.stderr)

    print(
        f"new Airfoil({alpha_min}f, {alpha_max}f, {expected_step}f, {java_float_array_init(cl)}, {java_float_array_init(cd)})")


if __name__ == "__main__":
    main()
