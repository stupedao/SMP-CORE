# SMPCORE

Advanced Minecraft SMP server optimization plugin with IP whitelist, sleep system, resource optimization, and LagFixer features.

## Features

### IP Whitelist System
- Only players with pre-registered IPs can join
- Strict enforcement (no bypass for operators)
- In-game command management
- Comprehensive logging
- UUID-based storage

### Auto-IP Registration
- Automatically registers player IPs on first join
- Enable/disable via commands
- Status monitoring
- Reset functionality for players

### IP Bypass System
- Add players to bypass IP whitelist
- Remove players from bypass list
- View all bypassed players

### Sleep System
- One-player sleep triggers day change
- Configurable 3-second delay
- Time change only (no weather changes)
- Silent operation
- World independent

### Server Optimization
- Entity limiting (max 25 per chunk)
- Chunk optimization (max 5000 loaded chunks)
- Item stacking
- Mob spawn control
- Redstone optimization
- TPS monitoring and recovery

### Ping & Network Optimization
- Continuous ping monitoring
- High ping warnings (1500ms)
- High ping kicking (2000ms)
- Network optimization
- Packet handling optimization

### Spark Profiler
- Real-time TPS monitoring (1m, 5m, 15m)
- Memory usage tracking
- Entity and chunk counts
- Uptime monitoring
- Enable/disable functionality
- Manual garbage collection trigger

### RAM & CPU Optimization
- Resource monitoring
- Automatic resource management
- Aggressive chunk unloading
- Non-essential feature disabling
- Garbage collection
- Memory cleanup

## Commands

### Main Commands
- `/smp` - Main SMPCORE command (alias: `/smpcore`)
- `/ping` - Check ping
- `/spark` - Spark profiler

### Subcommands
- `/smp help` - Show help menu
- `/smp addip <player>` - Add player to IP whitelist
- `/smp removeip <player>` - Remove player from IP whitelist
- `/smp bypassip add <player>` - Add player to IP bypass list
- `/smp bypassip remove <player>` - Remove player from IP bypass list
- `/smp bypassip list` - List all bypassed players
- `/smp listip` - List all whitelisted IPs
- `/smp autoip status` - Show auto-IP status
- `/smp autoip enable` - Enable auto-IP registration
- `/smp autoip disable` - Disable auto-IP registration
- `/smp autoip reset` - Reset auto-IP for player
- `/smp togglejoin [join|quit|both|show]` - Toggle join/quit messages
- `/smp reload` - Reload plugin configuration
- `/smp optimize` - Manual optimization trigger
- `/smp optimize status` - Show optimization status
- `/smp optimize tps` - Show current TPS
- `/smp optimize entities` - Show entity counts
- `/smp resources` - Show current RAM and CPU usage
- `/smp resources history` - Show resource usage history
- `/smp resources gc` - Force garbage collection
- `/smp resources cleanup` - Force memory cleanup
- `/smp resources stats` - Show detailed resource statistics
- `/spark tps` - Show current TPS
- `/spark gc` - Run garbage collection
- `/spark enable` - Enable spark profiler
- `/spark disable` - Disable spark profiler
- `/ping <player>` - Show player's ping
- `/ping top` - Show highest ping players
- `/ping stats` - Show ping statistics

## Permissions

- `smpcore.use` - Basic permission to use SMPCORE commands (default: true)
- `smpcore.admin` - Admin permissions for SMPCORE (default: op)
- `smpcore.ping` - Permission to use ping command (default: true)
- `smpcore.spark` - Permission to use spark profiler (default: true)

## Installation

1. Place the SMPCORE.jar file in your server's `plugins` folder
2. Restart the server
3. Configure the plugin in `plugins/SMPCORE/config.yml`
4. Reload the server or use `/smp reload`

## Configuration

The plugin creates a `config.yml` file with all configurable options:

### IP Whitelist
- `ip-whitelist.enabled` - Enable/disable IP whitelist
- `ip-whitelist.kick-message` - Message shown to kicked players
- `ip-whitelist.log-attempts` - Log join attempts
- `ip-whitelist.whitelist` - Whitelisted players

### Auto-IP
- `auto-ip.enabled` - Enable/disable auto-IP registration

### Bypass
- `bypass-ip.players` - List of players bypassed from IP check

### Sleep System
- `sleep-system.enabled` - Enable/disable sleep system
- `sleep-system.delay-seconds` - Delay before day change

### Optimization
- `optimization.enabled` - Enable/disable optimization
- `optimization.aggressive-mode` - Enable aggressive optimization
- Various optimization settings for entities, chunks, items, mobs, redstone, and TPS

### Ping Optimization
- `ping-optimization.enabled` - Enable/disable ping optimization
- Settings for ping monitoring, high ping management, and network optimization

### Spark Profiler
- `spark-profiler.enabled` - Enable/disable spark profiler

### Resource Optimization
- `resource-optimization.enabled` - Enable/disable resource optimization
- Settings for resource monitoring, thresholds, automatic management, and memory cleanup

## Requirements

- Minecraft 1.20.x or higher
- Java 17 or higher
- Spigot/Paper server

## Author

Stupedo

## Version

1.1.0

## License

This plugin is provided as-is for use on Minecraft servers.