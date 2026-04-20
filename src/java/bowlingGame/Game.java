import java.util.ArrayList;
import java.util.List;

public class Game {

    private static final int MAX_PINS = 10;
    private static final int TOTAL_FRAMES = 10;
    private static final int NORMAL_FRAMES = 9;

    private final List<Integer> rolls;
    private boolean scored = false;

    public Game() {
        this.rolls = new ArrayList<>();
    }

    public void roll(int pins) {
        validatePins(pins);

        if (isGameComplete()) {
            throw new IllegalStateException("Cannot roll after the game is complete.");
        }

        validateFrameRules(pins);
        rolls.add(pins);
    }

    public int score() {
        if (!isGameComplete()) {
            throw new IllegalStateException("Cannot score an incomplete game.");
        }

        // Prevent multiple score calculations
        if (scored) {
            return calculateScore();
        }

        scored = true;
        return calculateScore();
    }

    private int calculateScore() {
        int score = 0;
        int rollIndex = 0;

        for (int frame = 0; frame < TOTAL_FRAMES; frame++) {
            if (isStrike(rollIndex)) {
                score += 10 + strikeBonus(rollIndex);
                rollIndex += 1;
            } else if (isSpare(rollIndex)) {
                score += 10 + spareBonus(rollIndex);
                rollIndex += 2;
            } else {
                score += sumOfBallsInFrame(rollIndex);
                rollIndex += 2;
            }
        }

        return score;
    }

    private void validatePins(int pins) {
        if (pins < 0 || pins > MAX_PINS) {
            throw new IllegalArgumentException(
                    String.format("Pins must be between 0 and %d, but got %d.", MAX_PINS, pins));
        }
    }

    private void validateFrameRules(int pins) {
        FrameInfo info = getCurrentFrameInfo();

        if (info.frameNumber < TOTAL_FRAMES) {
            validateNormalFrame(info, pins);
        } else {
            validateTenthFrame(info, pins);
        }
    }

    private void validateNormalFrame(FrameInfo info, int pins) {
        if (info.rollInFrame == 2 && info.firstRoll + pins > MAX_PINS) {
            throw new IllegalArgumentException(
                    String.format("Frame total cannot exceed %d pins. Got %d + %d = %d.",
                            MAX_PINS, info.firstRoll, pins, info.firstRoll + pins));
        }
    }

    private void validateTenthFrame(FrameInfo info, int pins) {
        int size = info.tenthFrameRolls.size();

        if (size == 0) {
            return; // First roll in tenth frame is always valid
        }

        if (size == 1) {
            int first = info.tenthFrameRolls.get(0);

            if (first != MAX_PINS && first + pins > MAX_PINS) {
                throw new IllegalArgumentException(
                        String.format("Tenth frame total cannot exceed %d unless first roll is a strike. Got %d + %d = %d.",
                                MAX_PINS, first, pins, first + pins));
            }
            return;
        }

        if (size == 2) {
            int first = info.tenthFrameRolls.get(0);
            int second = info.tenthFrameRolls.get(1);

            boolean strike = first == MAX_PINS;
            boolean spare = first != MAX_PINS && first + second == MAX_PINS;

            if (!strike && !spare) {
                throw new IllegalStateException(
                        "No bonus roll allowed in tenth frame without strike or spare.");
            }

            if (strike && second != MAX_PINS && second + pins > MAX_PINS) {
                throw new IllegalArgumentException(
                        String.format("Invalid bonus rolls after strike in tenth frame. Got %d + %d = %d.",
                                second, pins, second + pins));
            }

            if (spare && pins > MAX_PINS) {
                throw new IllegalArgumentException(
                        String.format("Bonus roll after spare cannot exceed %d pins. Got %d.",
                                MAX_PINS, pins));
            }

            return;
        }

        if (size >= 3) {
            throw new IllegalStateException("No more rolls allowed in tenth frame.");
        }
    }

    public boolean isGameComplete() {
        FrameInfo info = getCurrentFrameInfo();

        if (info.frameNumber < TOTAL_FRAMES) {
            return false;
        }

        int size = info.tenthFrameRolls.size();

        if (size < 2) {
            return false;
        }

        int first = info.tenthFrameRolls.get(0);
        int second = info.tenthFrameRolls.get(1);

        if (first == MAX_PINS || first + second == MAX_PINS) {
            return size == 3;
        }

        return size == 2;
    }

    private FrameInfo getCurrentFrameInfo() {
        int frame = 1;
        int index = 0;

        while (frame < TOTAL_FRAMES && index < rolls.size()) {
            int first = rolls.get(index);

            if (first == MAX_PINS) {
                index++;
            } else {
                if (index + 1 >= rolls.size()) {
                    return new FrameInfo(frame, 2, first, List.of());
                }
                index += 2;
            }
            frame++;
        }

        List<Integer> tenthFrameRolls = new ArrayList<>();
        while (index < rolls.size()) {
            tenthFrameRolls.add(rolls.get(index));
            index++;
        }

        return new FrameInfo(frame, 1, 0, tenthFrameRolls);
    }

    private boolean isStrike(int rollIndex) {
        return rolls.get(rollIndex) == MAX_PINS;
    }

    private boolean isSpare(int rollIndex) {
        return rolls.get(rollIndex) + rolls.get(rollIndex + 1) == MAX_PINS;
    }

    private int strikeBonus(int rollIndex) {
        return rolls.get(rollIndex + 1) + rolls.get(rollIndex + 2);
    }

    private int spareBonus(int rollIndex) {
        return rolls.get(rollIndex + 2);
    }

    private int sumOfBallsInFrame(int rollIndex) {
        return rolls.get(rollIndex) + rolls.get(rollIndex + 1);
    }

    private static class FrameInfo {
        private final int frameNumber;
        private final int rollInFrame;
        private final int firstRoll;
        private final List<Integer> tenthFrameRolls;

        private FrameInfo(int frameNumber, int rollInFrame, int firstRoll, List<Integer> tenthFrameRolls) {
            this.frameNumber = frameNumber;
            this.rollInFrame = rollInFrame;
            this.firstRoll = firstRoll;
            this.tenthFrameRolls = tenthFrameRolls;
        }
    }
}