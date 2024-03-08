package grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.pl.StellaType;

import java.util.HashMap;

public class StellaRuleContext extends ParserRuleContext {
    public HashMap<String, StellaType> localVariables = new HashMap<>();
    public StellaType expected = null;

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
