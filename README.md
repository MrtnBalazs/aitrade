
### AITrader

AITrader is an application which makes daily predictions on where the given stock prices are moving, and makes it available through an API.
The prediction is a number which if positive, that means the price will go up. If it's negative that means price will go down. The absoulte value
of the number tells us how good is the prediction (the bigger the better).

## Usage

AITrader can run in 3 different modes: initialization, regular, test. ***First time you have to start it in initialization mode!***
In initialization mode it needs a *.txt* file with the names and symbols of the stocks we want to calculate predictions on.
The start mode and the file name of the stock list have to be set in the *settings.txt* file.
The three start modes are INIT (for initialization), REG (for regular start) and TEST (for test start).

The application saves it's data to Microsoft SQL Server, and uses finnhub.io API to request the stock candle data on every workday at 16:15 (US/Eastern time).

The data from the SQL server is available through these routes:

- One stock: http://localhost:8090/stock/{stockid}
- All stocks: http://localhost:8090/stocks
- Candle data for given stock: http://localhost:8090/candles/{stockid}
- Indicator data for given stock: http://localhost:8090/indicators/{stockid}
- Pattern data for given stock: http://localhost:8090/pattern/{stockid}
- Prediciton data for given stock: http://localhost:8090/predictions/{stockid}
- Logs: http://localhost:8090/logs
