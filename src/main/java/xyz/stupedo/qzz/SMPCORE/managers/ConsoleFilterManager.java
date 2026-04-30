package xyz.stupedo.qzz.SMPCORE.managers;

import xyz.stupedo.qzz.SMPCORE.SMPCORE;
import xyz.stupedo.qzz.SMPCORE.utils.ConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConsoleFilterManager {

    private final SMPCORE plugin;
    private final List<FilterRule> filterRules;
    private int messagesFiltered;

    public ConsoleFilterManager(SMPCORE plugin) {
        this.plugin = plugin;
        this.filterRules = new ArrayList<>();
        this.messagesFiltered = 0;
        loadConfiguration();
    }

    private void loadConfiguration() {
        if (!ConfigUtils.getBoolean("console-filter.enabled", true)) {
            return;
        }

        List<Map<?, ?>> rules = ConfigUtils.getConfig().getMapList("console-filter.filter-rules");
        if (rules != null) {
            for (Map<?, ?> rule : rules) {
                Object patternObj = rule.get("pattern");
                Object actionObj = rule.get("action");

                if (patternObj != null && actionObj != null) {
                    String pattern = patternObj.toString();
                    String action = actionObj.toString();

                    try {
                        FilterRule filterRule = new FilterRule(pattern, action);
                        filterRules.add(filterRule);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid console filter rule: " + rule);
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + filterRules.size() + " console filter rules");
    }

    public boolean isEnabled() {
        return ConfigUtils.getBoolean("console-filter.enabled", true);
    }

    public boolean shouldFilterMessage(String message) {
        if (!isEnabled()) {
            return false;
        }

        for (FilterRule rule : filterRules) {
            if (rule.matches(message)) {
                if ("hide".equals(rule.action)) {
                    messagesFiltered++;
                    return true;
                }
            }
        }

        return false;
    }

    public String filterMessage(String message) {
        if (!isEnabled()) {
            return message;
        }

        for (FilterRule rule : filterRules) {
            if (rule.matches(message)) {
                if ("hide".equals(rule.action)) {
                    messagesFiltered++;
                    return null;
                } else if ("replace".equals(rule.action)) {
                    messagesFiltered++;
                    return "[Filtered]";
                }
            }
        }

        return message;
    }

    public int getMessagesFiltered() {
        return messagesFiltered;
    }

    public void resetStatistics() {
        messagesFiltered = 0;
    }

    public List<FilterRule> getFilterRules() {
        return new ArrayList<>(filterRules);
    }

    public static class FilterRule {
        private final Pattern pattern;
        private final String action;

        public FilterRule(String pattern, String action) {
            this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            this.action = action.toLowerCase();
        }

        public boolean matches(String message) {
            return pattern.matcher(message).find();
        }

        public String getAction() {
            return action;
        }
    }
}