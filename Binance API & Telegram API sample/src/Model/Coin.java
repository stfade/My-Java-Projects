package Model;

public class Coin {
    private String coinName;
    private double high;
    private double low;
    private boolean state;

    public Coin(String coinName, double high, double low, boolean state) {
        this.coinName = coinName;
        this.high = high;
        this.low = low;
        this.state = state;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
