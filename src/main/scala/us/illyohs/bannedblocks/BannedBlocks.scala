/**
  * Copyright (c) 2016, Anthony Anderson(Illyohs)
  * All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
package us.illyohs.bannedblocks

import java.io.{BufferedReader, File, FileReader}
import java.util

import scala.util.control._

import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.{ChangeBlockEvent, InteractBlockEvent}
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.plugin.Plugin

@Plugin(
  id = "bannedblocks",
  name = "Banned Blocks",
  version = "1.0.0",
  description = "Handles banned blocks and prevents their placement",
  authors = Array("Anthony Anderson<Illyohs>")
)
class BannedBlocks {

  @Inject
  val log:Logger = null

  val banned:util.List[String] = new util.ArrayList[String]()

  @Listener
  def serverStarting(e:GameStartingServerEvent): Unit = {
    initFiles
  }


  @Listener
  def blockBreakEvent(e:ChangeBlockEvent.Place, @First player:Player): Unit = {
    if (!player.hasPermission("bb.override.place")) {
      import scala.collection.JavaConversions._
      for (t <- e.getTransactions) {
        val loc = t.getFinal.getLocation.get()
        if (isBanned(e.getTargetWorld.getBlock(loc.getBlockPosition).getType)) {
          e.setCancelled(true)
        }
      }
    }
  }

  @Listener
  def blockInteractEvent(e:InteractBlockEvent, @First player:Player): Unit = {
    if (!player.hasPermission("bb.override.interact")) {
      if (isBanned(e.getTargetBlock.getState.getType)) {
        e.setCancelled(true)
      }
    }
  }

  def isBanned(blockType: BlockType): Boolean = {
    if (banned.contains(blockType.getName)) {
      true
    } else {
      false
    }
  }

  def initFiles: Unit = {
    val configDir = "./config/bannedblocks/"
    val confDir = new File(configDir)
    val cfg = new File(configDir + "blacklist.cfg")
    if (!confDir.exists()) {
      confDir.mkdir()
    }
    if (!cfg.exists()) {
      cfg.createNewFile()
    }

    val reader = new BufferedReader(new FileReader(cfg))

    val loop = new Breaks
    while (true) {
      val line = reader.readLine()
      if (line == null) loop.break()

      log.info("Adding " + line + "to blacklist")
      banned.add(line)
    }
    reader.close()
  }
}