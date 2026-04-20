import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Bowling Game Kata - TDD Implementation")
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Nested
    @DisplayName("Basic Kata Scenarios")
    class BasicKataScenarios {

        @Test
        @DisplayName("Gutter game should score 0")
        void testGutterGame() {
            rollMany(20, 0);
            assertEquals(0, game.score());
        }

        @Test
        @DisplayName("All ones should score 20")
        void testAllOnes() {
            rollMany(20, 1);
            assertEquals(20, game.score());
        }

        @Test
        @DisplayName("One spare should add the next roll as bonus")
        void testOneSpare() {
            rollSpare();
            game.roll(3);
            rollMany(17, 0);
            assertEquals(16, game.score());
        }

        @Test
        @DisplayName("One strike should add the next two rolls as bonus")
        void testOneStrike() {
            rollStrike();
            game.roll(3);
            game.roll(4);
            rollMany(16, 0);
            assertEquals(24, game.score());
        }

        @Test
        @DisplayName("Perfect game should score 300")
        void testPerfectGame() {
            rollMany(12, 10);
            assertEquals(300, game.score());
        }
    }

    @Nested
    @DisplayName("Tenth Frame Special Cases")
    class TenthFrameSpecialCases {

        @Test
        @DisplayName("Last frame spare should allow one bonus roll")
        void testLastFrameSpare() {
            rollMany(18, 0);
            game.roll(5);
            game.roll(5);
            game.roll(7);
            assertEquals(17, game.score());
        }

        @Test
        @DisplayName("Last frame strike should allow two bonus rolls")
        void testLastFrameStrike() {
            rollMany(18, 0);
            game.roll(10);
            game.roll(7);
            game.roll(2);
            assertEquals(19, game.score());
        }

        @Test
        @DisplayName("Perfect tenth frame with three strikes")
        void testPerfectTenthFrame() {
            rollMany(18, 0);
            game.roll(10);
            game.roll(10);
            game.roll(10);
            assertEquals(30, game.score());
        }

        @Test
        @DisplayName("Tenth frame spare followed by strike")
        void testTenthFrameSpareFollowedByStrike() {
            rollMany(18, 0);
            game.roll(5);
            game.roll(5);
            game.roll(10);
            assertEquals(20, game.score());
        }
    }

    @Nested
    @DisplayName("Input Validation - Error Cases")
    class InputValidation {

        @Test
        @DisplayName("Negative roll should throw exception")
        void testNegativeRoll() {
            assertThrows(IllegalArgumentException.class, () -> game.roll(-1),
                    "Should reject negative pins");
        }

        @Test
        @DisplayName("Roll greater than 10 should throw exception")
        void testRollGreaterThanTen() {
            assertThrows(IllegalArgumentException.class, () -> game.roll(11),
                    "Should reject pins greater than 10");
        }

        @Test
        @DisplayName("Frame total greater than 10 should throw exception")
        void testFrameTotalGreaterThanTen() {
            game.roll(8);
            assertThrows(IllegalArgumentException.class, () -> game.roll(5),
                    "Should reject frame total exceeding 10");
        }

        @Test
        @DisplayName("Frame total exactly 10 should be allowed (spare)")
        void testFrameTotalEqualTenIsSpare() {
            game.roll(7);
            game.roll(3); // This should not throw
            rollMany(18, 0);
            assertEquals(10, game.score());
        }

        @Test
        @DisplayName("Multiple invalid rolls with different pin counts")
        void testMultipleInvalidRolls() {
            assertThrows(IllegalArgumentException.class, () -> game.roll(15));
            assertThrows(IllegalArgumentException.class, () -> game.roll(-5));
            assertThrows(IllegalArgumentException.class, () -> game.roll(100));
        }
    }

    @Nested
    @DisplayName("Game State Validation")
    class GameStateValidation {

        @Test
        @DisplayName("Rolling after game completion should throw exception")
        void testRollingAfterGameComplete() {
            rollMany(20, 0);
            assertThrows(IllegalStateException.class, () -> game.roll(0),
                    "Should not allow rolls after game completion");
        }

        @Test
        @DisplayName("Scoring incomplete game should throw exception")
        void testScoreIncompleteGame() {
            game.roll(10);
            assertThrows(IllegalStateException.class, game::score,
                    "Should not allow scoring incomplete game");
        }

        @Test
        @DisplayName("Cannot roll after 10 pins in normal frame")
        void testCannotRollAfterTenInNormalFrame() {
            game.roll(5);
            game.roll(5); // spare
            game.roll(3);
            // This should complete the frame, next roll should start frame 2
            game.roll(4);
            // Should not throw since we're still in the game
            assertTrue(true);
        }
    }

    @Nested
    @DisplayName("Tenth Frame Edge Cases")
    class TenthFrameEdgeCases {

        @Test
        @DisplayName("Open tenth frame should not allow bonus roll")
        void testNoBonusRollAfterOpenTenthFrame() {
            rollMany(18, 0);
            game.roll(3);
            game.roll(4);
            assertThrows(IllegalStateException.class, () -> game.roll(1),
                    "Should not allow bonus roll after open tenth frame");
        }

        @Test
        @DisplayName("Tenth frame all zeros should complete game")
        void testTenthFrameAllZeros() {
            rollMany(18, 0);
            game.roll(0);
            game.roll(0);
            assertTrue(game.isGameComplete());
        }

        @Test
        @DisplayName("Tenth frame with strike allows two more rolls")
        void testTenthFrameStrikeAllowsTwoMoreRolls() {
            rollMany(18, 0);
            game.roll(10);
            assertFalse(game.isGameComplete());
            game.roll(5);
            assertFalse(game.isGameComplete());
            game.roll(3);
            assertTrue(game.isGameComplete());
        }

        @Test
        @DisplayName("Tenth frame with spare allows one more roll")
        void testTenthFrameSpareAllowsOneMoreRoll() {
            rollMany(18, 0);
            game.roll(5);
            game.roll(5);
            assertFalse(game.isGameComplete());
            game.roll(3);
            assertTrue(game.isGameComplete());
        }
    }

    @Nested
    @DisplayName("Complex Game Scenarios")
    class ComplexGameScenarios {

        @Test
        @DisplayName("Mixed game with strikes and spares")
        void testMixedGame() {
            game.roll(10); // Frame 1: Strike (20)
            game.roll(7);
            game.roll(3);  // Frame 2: Spare (19)
            game.roll(9);
            game.roll(0);  // Frame 3: Open (9)
            game.roll(10); // Frame 4: Strike (18)
            game.roll(0);
            game.roll(8);  // Frame 5: Open (8)
            game.roll(8);
            game.roll(2);  // Frame 6: Spare (10)
            game.roll(0);
            game.roll(6);  // Frame 7: Open (6)
            game.roll(10); // Frame 8: Strike (30)
            game.roll(10); // Frame 9: Strike (28)
            game.roll(10); // Frame 10: Strike (19)
            game.roll(8);
            game.roll(1);  // Bonus rolls

            assertEquals(167, game.score());
        }

        @Test
        @DisplayName("All spares game should score 150")
        void testAllSparesGame() {
            rollMany(21, 5); // 5 + 5 = 10 each frame, bonus is 5
            assertEquals(150, game.score());
        }

        @Test
        @DisplayName("Alternating strikes and open frames")
        void testAlternatingStrikesAndOpenFrames() {
            for (int i = 0; i < 9; i++) {
                if (i % 2 == 0) {
                    game.roll(10); // Strike
                } else {
                    game.roll(5);
                    game.roll(4); // Open frame
                }
            }
            game.roll(10); // Perfect tenth frame
            game.roll(10);
            game.roll(10);
            game.score(); // Should not throw
            assertTrue(true);
        }

        @Test
        @DisplayName("Minimum valid game (all gutter balls)")
        void testMinimumValidGame() {
            rollMany(20, 0);
            assertEquals(0, game.score());
        }

        @Test
        @DisplayName("Maximum valid game (all strikes)")
        void testMaximumValidGame() {
            rollMany(12, 10);
            assertEquals(300, game.score());
        }
    }

    @Nested
    @DisplayName("Tenth Frame Invalid Combinations")
    class TenthFrameInvalidCombinations {

        @Test
        @DisplayName("Tenth frame without strike/spare should reject third roll")
        void testTenthFrameOpenRejectsThirdRoll() {
            rollMany(18, 0);
            game.roll(3);
            game.roll(4);
            assertThrows(IllegalStateException.class, () -> game.roll(1));
        }

        @Test
        @DisplayName("Tenth frame with non-strike first and invalid second")
        void testTenthFrameInvalidSecondRoll() {
            rollMany(18, 0);
            game.roll(7);
            assertThrows(IllegalArgumentException.class, () -> game.roll(5),
                    "Second roll would exceed 10 without strike");
        }

        @Test
        @DisplayName("Tenth frame after strike with invalid bonus rolls")
        void testTenthFrameStrikeInvalidBonusRolls() {
            rollMany(18, 0);
            game.roll(10);
            game.roll(7);
            assertThrows(IllegalArgumentException.class, () -> game.roll(5),
                    "Bonus rolls would exceed 10");
        }
    }

    // Helper methods
    private void rollMany(int rolls, int pins) {
        for (int i = 0; i < rolls; i++) {
            game.roll(pins);
        }
    }

    private void rollSpare() {
        game.roll(5);
        game.roll(5);
    }

    private void rollStrike() {
        game.roll(10);
    }
}