package net.defracted.adminutils.commands

import net.defracted.adminutils.Main
import net.defracted.adminutils.util.Formatters.chat
import net.defracted.adminutils.util.Formatters.isStrNum

import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

import java.util.*


// Нужно завершить
class Ban(private val plugin: Main) : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (cmd.name.equals("ban", ignoreCase = true)) {
            if (args.isEmpty()) {
                sender.sendMessage(chat("&cВы не указали ник!"))
                return true
            }

            // Получаем цель бана
            val target = args[0]

            // Проверяем наличие длительности
            if (args.size < 2) {
                sender.sendMessage(chat("&cВы не указали длительность бана."))
                return true
            }

            // Получаем срок бана (в минутах)
            val punishmentDuration: Int = if (isStrNum(args[1])) {
                args[1].toInt()
            } else {
                sender.sendMessage(chat("&cУкажите срок действия бана в минутах!"))
                return true
            }

            // Создаём дату, основываясь на указанном времени
            val punishmentDateExpiration: Date?
            punishmentDateExpiration = if (punishmentDuration > 0) {
                Date(System.currentTimeMillis() + punishmentDuration * 60000)
            } else {
                null
            }

            // Получение причины из аргументов
            var reason = ""
            if (args.size <= 2) {
                reason += "не указана."
            } else {
                for (i in 2 until args.size) reason += args[i] + if (i != args.size - 1) " " else ""
            }
            val infoExpiration: String
            infoExpiration = if (punishmentDuration <= 0) {
                "&fнавсегда"
            } else {
                "&fдо " + punishmentDateExpiration.toString()
            }

            // Кикаем цель, но если игрок в онлайне
            val playerTarget = Bukkit.getPlayerExact(target)

            // Баним цель на указанный срок
            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason, punishmentDateExpiration, sender.name)

            // Кикаем игрока, если он в онлайне
            playerTarget?.kickPlayer(chat("&cВы были забанены!\n\n&fПереподключитесь, чтобы узнать подробности."))

            // Информируем модератора
            sender.sendMessage(chat("&aИгрок &f$target&a забанен $infoExpiration &aпо причине: &f$reason"))

            // Информируем всех игроков
            if (plugin.config.getBoolean("broadcast_ban_message")) Bukkit.broadcastMessage(chat("""
    &c&lОдин из игроков был удалён из игры за использование читов или другие нарушения. 
    &bБлагодарим вас за жалобы!
    """.trimIndent()))
            return true
        }
        return false
    }

}