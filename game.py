class BowlingError(Exception):
    pass


class Game:
    MAX_FRAMES = 10
    MAX_PINS = 10

    def __init__(self):
        self._rolls = []
        self._current_roll = 0

    def roll(self, pins):
        if not isinstance(pins, int) or isinstance(pins, bool):
            raise BowlingError("Pins must be an integer")
        if pins < 0:
            raise BowlingError("Pins cannot be negative")
        if pins > self.MAX_PINS:
            raise BowlingError(f"Pins cannot exceed {self.MAX_PINS}")
        if self._is_game_over():
            raise BowlingError("Game is already over, cannot roll anymore")

        self._validate_frame_pins(pins)
        self._rolls.append(pins)
        self._current_roll += 1

    def _validate_frame_pins(self, pins):
        roll_index = 0
        for frame in range(self.MAX_FRAMES):
            if roll_index >= len(self._rolls):
                break
            if self._is_strike_at(roll_index):
                roll_index += 1
            else:
                first = self._rolls[roll_index]
                if roll_index + 1 == len(self._rolls):
                    if frame < self.MAX_FRAMES - 1 and first + pins > self.MAX_PINS:
                        raise BowlingError(
                            "Pin count exceeds pins on the lane"
                        )
                    break
                roll_index += 2

    def score(self):
        if not self._is_game_complete():
            raise BowlingError("Game is not yet complete")
        total = 0
        roll_index = 0
        for _ in range(self.MAX_FRAMES):
            if self._is_strike_at(roll_index):
                total += self.MAX_PINS + self._strike_bonus(roll_index)
                roll_index += 1
            elif self._is_spare_at(roll_index):
                total += self.MAX_PINS + self._spare_bonus(roll_index)
                roll_index += 2
            else:
                total += self._frame_score(roll_index)
                roll_index += 2
        return total

    def _is_strike_at(self, roll_index):
        return self._rolls[roll_index] == self.MAX_PINS

    def _is_spare_at(self, roll_index):
        return (
            self._rolls[roll_index] + self._rolls[roll_index + 1]
            == self.MAX_PINS
        )

    def _strike_bonus(self, roll_index):
        return self._rolls[roll_index + 1] + self._rolls[roll_index + 2]

    def _spare_bonus(self, roll_index):
        return self._rolls[roll_index + 2]

    def _frame_score(self, roll_index):
        return self._rolls[roll_index] + self._rolls[roll_index + 1]

    def _is_game_over(self):
        return self._is_game_complete()

    def _is_game_complete(self):
        roll_index = 0
        for frame in range(self.MAX_FRAMES):
            if roll_index >= len(self._rolls):
                return False
            if frame == self.MAX_FRAMES - 1:
                if self._is_strike_at(roll_index):
                    return len(self._rolls) - roll_index >= 3
                if roll_index + 1 < len(self._rolls) and self._is_spare_at(
                    roll_index
                ):
                    return len(self._rolls) - roll_index >= 3
                return len(self._rolls) - roll_index >= 2
            if self._is_strike_at(roll_index):
                roll_index += 1
            else:
                roll_index += 2
        return True
