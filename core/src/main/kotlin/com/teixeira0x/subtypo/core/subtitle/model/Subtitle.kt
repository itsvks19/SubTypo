package com.teixeira0x.subtypo.core.subtitle.model

import com.teixeira0x.subtypo.core.subtitle.format.SRTFormat
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat

data class Subtitle(
  val id: Long = 0,
  val projectId: Long,
  val name: String,
  val format: SubtitleFormat = SRTFormat,
  val cues: List<Cue>,
)
