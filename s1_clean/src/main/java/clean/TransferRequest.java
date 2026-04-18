package clean;

import java.math.BigDecimal;

public class TransferRequest {
    private Long outAccId;
    private Long inAccId;
    private BigDecimal amt;

    public Long getOutAccId() {
        return outAccId;
    }

    public void setOutAccId(Long outAccId) {
        this.outAccId = outAccId;
    }

    public Long getInAccId() {
        return inAccId;
    }

    public void setInAccId(Long inAccId) {
        this.inAccId = inAccId;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

}
