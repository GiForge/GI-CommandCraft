package com.example

import com.example.totalcommander.config.IniConfigManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testTotalCmdIniParserAndSerializer() {
    val defaultIni = IniConfigManager.generateDefaultIni()
    val config = IniConfigManager.parseIni(defaultIni)
    assertEquals(14, config.fontSize)
    assertEquals("classic_blue", config.themePreset)
    assertTrue(config.showHiddenFiles)
    assertTrue(config.confirmDelete)
    assertEquals("tabs", config.dualPaneMode)

    val serialized = IniConfigManager.serializeIni(config)
    val parsedAgain = IniConfigManager.parseIni(serialized)
    assertEquals(config.fontSize, parsedAgain.fontSize)
    assertEquals(config.themePreset, parsedAgain.themePreset)
    assertEquals(config.showHiddenFiles, parsedAgain.showHiddenFiles)
  }
}
