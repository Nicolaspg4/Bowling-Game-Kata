import unittest

from game import BowlingError, Game


class TestBowlingGame(unittest.TestCase):
    """TDD tests for the Bowling Game Kata.

    Tests follow the classic kata progression:
      1. Gutter game
      2. All ones
      3. One spare
      4. One strike
      5. Perfect game
    Plus additional fault-tolerance tests.
    """

    def setUp(self):
        self.game = Game()

    # ------------------------------------------------------------------
    # Helper methods
    # ------------------------------------------------------------------
    def _roll_many(self, n, pins):
        for _ in range(n):
            self.game.roll(pins)

    def _roll_spare(self):
        self.game.roll(5)
        self.game.roll(5)

    def _roll_strike(self):
        self.game.roll(10)

    # ------------------------------------------------------------------
    # Classic kata tests
    # ------------------------------------------------------------------
    def test_gutter_game(self):
        """All rolls knock down 0 pins → score = 0."""
        self._roll_many(20, 0)
        self.assertEqual(0, self.game.score())

    def test_all_ones(self):
        """All rolls knock down 1 pin → score = 20."""
        self._roll_many(20, 1)
        self.assertEqual(20, self.game.score())

    def test_one_spare(self):
        """One spare in first frame, next roll = 3, rest gutter → score = 16."""
        self._roll_spare()       # frame 1: 5+5 = spare
        self.game.roll(3)        # bonus for spare
        self._roll_many(17, 0)   # remaining frames
        self.assertEqual(16, self.game.score())

    def test_one_strike(self):
        """One strike in first frame, next two rolls = 3+4, rest gutter → score = 24."""
        self._roll_strike()      # frame 1: strike
        self.game.roll(3)        # bonus roll 1
        self.game.roll(4)        # bonus roll 2
        self._roll_many(16, 0)   # remaining frames
        self.assertEqual(24, self.game.score())

    def test_perfect_game(self):
        """12 strikes in a row → score = 300."""
        self._roll_many(12, 10)
        self.assertEqual(300, self.game.score())

    # ------------------------------------------------------------------
    # Extended / custom tests
    # ------------------------------------------------------------------
    def test_all_spares(self):
        """All spares with 5 pins each, final bonus roll = 5 → score = 150."""
        self._roll_many(21, 5)   # 10 frames × (5+5) + 1 bonus roll
        self.assertEqual(150, self.game.score())

    def test_multiple_strikes_in_a_row(self):
        """Three consecutive strikes followed by 3+4 and gutter fills → score = 60."""
        self._roll_strike()
        self._roll_strike()
        self._roll_strike()
        self.game.roll(3)
        self.game.roll(4)
        self._roll_many(12, 0)
        # Frame 1: 10 + 10 + 10 = 30
        # Frame 2: 10 + 10 + 3  = 23
        # Frame 3: 10 + 3  + 4  = 17
        # Frame 4:  3 + 4        = 7
        # Frames 5-10: 0
        self.assertEqual(77, self.game.score())

    def test_spare_in_last_frame(self):
        """Spare in the 10th frame allows one bonus roll."""
        self._roll_many(18, 0)
        self._roll_spare()    # 10th frame spare
        self.game.roll(7)     # bonus roll
        self.assertEqual(17, self.game.score())

    def test_strike_in_last_frame(self):
        """Strike in the 10th frame allows two bonus rolls."""
        self._roll_many(18, 0)
        self._roll_strike()   # 10th frame strike
        self.game.roll(3)
        self.game.roll(4)
        self.assertEqual(17, self.game.score())

    def test_two_strikes_in_last_frame(self):
        """Two strikes in the 10th frame (plus one bonus roll)."""
        self._roll_many(18, 0)
        self._roll_strike()
        self._roll_strike()
        self.game.roll(5)
        self.assertEqual(25, self.game.score())

    # ------------------------------------------------------------------
    # Fault-tolerance / input validation tests
    # ------------------------------------------------------------------
    def test_negative_pins_raises(self):
        """Rolling a negative number of pins raises BowlingError."""
        with self.assertRaises(BowlingError):
            self.game.roll(-1)

    def test_pins_exceed_max_raises(self):
        """Rolling more than 10 pins raises BowlingError."""
        with self.assertRaises(BowlingError):
            self.game.roll(11)

    def test_pins_exceed_remaining_in_frame_raises(self):
        """Sum of two rolls in a frame cannot exceed 10 (outside 10th frame)."""
        self.game.roll(6)
        with self.assertRaises(BowlingError):
            self.game.roll(5)  # 6 + 5 = 11 → invalid

    def test_roll_after_game_over_raises(self):
        """Rolling after the game is complete raises BowlingError."""
        self._roll_many(20, 0)
        with self.assertRaises(BowlingError):
            self.game.roll(0)

    def test_score_before_game_complete_raises(self):
        """Calling score() before the game is finished raises BowlingError."""
        self._roll_many(10, 0)
        with self.assertRaises(BowlingError):
            self.game.score()

    def test_non_integer_pins_raises(self):
        """Passing a non-integer value raises BowlingError."""
        with self.assertRaises(BowlingError):
            self.game.roll("three")

    def test_float_pins_raises(self):
        """Passing a float value raises BowlingError."""
        with self.assertRaises(BowlingError):
            self.game.roll(1.5)

    def test_boolean_pins_raises(self):
        """Passing a boolean raises BowlingError (bool is subclass of int)."""
        with self.assertRaises(BowlingError):
            self.game.roll(True)

    def test_zero_score_valid(self):
        """Rolling 0 is valid and does not raise."""
        self.game.roll(0)  # should not raise

    def test_max_pins_valid(self):
        """Rolling exactly 10 pins (a strike) is valid."""
        self.game.roll(10)  # should not raise


if __name__ == "__main__":
    unittest.main()
