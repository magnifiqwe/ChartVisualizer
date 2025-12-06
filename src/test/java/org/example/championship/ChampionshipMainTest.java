package org.example.championship;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class ChampionshipMainTest {

    @Test
    void testMainRunsWithoutException() {
        // main() просто выводит в консоль, никакого UI не открывает.
        // Проверяем, что он отрабатывает полностью.
        assertDoesNotThrow(() -> ChampionshipMain.main(new String[]{}));
    }
}
