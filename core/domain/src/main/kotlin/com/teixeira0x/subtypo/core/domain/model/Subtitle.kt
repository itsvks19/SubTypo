package com.teixeira0x.subtypo.core.domain.model

import com.teixeira0x.subtypo.core.domain.subtitle.format.SRTFormat
import com.teixeira0x.subtypo.core.domain.subtitle.format.SubtitleFormat

data class Subtitle(
  val id: Long = 0,
  val projectId: Long,
  val name: String,
  val format: SubtitleFormat = SRTFormat,
  val cues: List<Cue>,
)
