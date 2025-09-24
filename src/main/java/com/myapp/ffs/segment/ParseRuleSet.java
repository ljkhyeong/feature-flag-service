package com.myapp.ffs.segment;

import java.util.List;

public record ParseRuleSet(List<Condition> conditions, Logic logic) {
}
