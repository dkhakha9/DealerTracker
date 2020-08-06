-- Inventory

var make varchar2(12);
--exec :make := '%honda%';
--exec :make := '%toyota%';
exec :make := '%nissan%';

set linesize 250;
--set pagesize 10640;
set pagesize 0;
set colsep ,
set trimspool on

set echo off;

spool C:\xaxa\DBMS\autohelper\stock.csv

select dealer_name, date_arrived, newcars.vin, modelyear, make, model, ADDLINFO from newcars, vehicles, dealerweb
where lower(newcars.vin) = lower(vehicles.vin) and dealerweb.dealer_id = newcars.dealer_id and date_sold is null and lower(dealer_name) like :make
order by date_arrived ASC;

spool off;

-- Sales

var make varchar2(12);
var start_date varchar2(9);
exec :start_date := '01-aug-17';
--exec :make := '%honda%';
exec :make := '%toyota%';
--exec :make := '%nissan%';

set linesize 250;
--set pagesize 10640;
set pagesize 0;
set colsep ,
set trimspool on

set echo off;

spool C:\xaxa\DBMS\autohelper\stock.csv

select dealer_name, date_arrived, date_sold, newcars.vin, modelyear, make, model, ADDLINFO from newcars, vehicles, dealerweb
where lower(newcars.vin) = lower(vehicles.vin) and dealerweb.dealer_id = newcars.dealer_id and date_sold > :start_date and lower(dealer_name) like :make
order by date_sold ASC;

spool off;