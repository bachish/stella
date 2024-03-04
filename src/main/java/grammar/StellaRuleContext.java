package grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
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

    public <T> T accept(ParseTreeVisitor<? extends T> visitor, StellaType expected) {
        updateLocals();
        this.expected = expected;
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
        return accept(visitor, null);
    }


}
