package com.example.itrade.business;

public class Settings {
    StartMode startMode = null;
    String stockListFileName = "";

    public Settings(StartMode startMode, String stockListFileName){
        this.startMode = startMode;
        this.stockListFileName = stockListFileName;
    }

    public Settings(){}

    public boolean isValid(){
        if(startMode == null)
            return false;
        else if (startMode == StartMode.INIT && stockListFileName.equals(""))
            return false;
        else
            return true;
    }

    public StartMode getStartMode() {
        return startMode;
    }

    public String getStockListFileName() {
        return stockListFileName;
    }

    public void setStartMode(StartMode startMode) {this.startMode = startMode;}

    public void setStockListFileName(String stockListFileName) {this.stockListFileName = stockListFileName;}
}
