/* (C) Sandro Koch 2021 */
package de.layornos.game.pong;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AppTest {
  @Test
  void appHasAGreeting() {
    App classUnderTest = new App();
    assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
  }
}
