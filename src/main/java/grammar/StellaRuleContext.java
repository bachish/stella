package grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.pl.StellaType;
import org.pl.TypeCheckingError;

import java.util.HashMap;
import java.util.HashSet;

public class StellaRuleContext extends ParserRuleContext {
    public HashMap<String, StellaType> localVariables = new HashMap<>();
    public StellaType expected = null;

    /**
     * In pattern  matching we have two types:
     * 1. type of pattern (type of matching argument)
     * 2. type of pattern-matching body (type of matching result)
     * This field contains a first type
     */
    public StellaType patternExpectedType = null;

    public StellaRuleContext() {
    }

    public StellaRuleContext(ParserRuleContext parent, int invokingStateNumber) {
        super(parent, invokingStateNumber);
    }

    public void updateLocals() {
        StellaRuleContext parentContext = (StellaRuleContext) parent;
        localVariables.putAll(parentContext.localVariables);
    }


    public void addToParentCtx(String name, StellaType type) {
        ((StellaRuleContext) parent).localVariables.put(name, type);
    }

    public void addAllToParentCtx() {
        ((StellaRuleContext) parent).localVariables.putAll(localVariables);
    }


}
