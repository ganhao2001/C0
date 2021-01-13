
package c0.error;

import c0.util.Pos;

public class TokenizeError extends c0.error.CompileError {
    // auto-generated
    private static final long serialVersionUID = 1L;

    private c0.error.ErrorCode err;
    private Pos pos;

    public TokenizeError(c0.error.ErrorCode err, Pos pos) {
        super();
        this.err = err;
        this.pos = pos;
    }

    public TokenizeError(c0.error.ErrorCode err, Integer row, Integer col) {
        super();
        this.err = err;
        this.pos = new Pos(row, col);
    }

    public c0.error.ErrorCode getErr() {
        return err;
    }

    public Pos getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Tokenize Error: ").append(err).append(", at: ").append(pos).toString();
    }
}
