var start_date varchar2(9);
var make varchar2(12);
exec :start_date := '01-sep-17';
exec :make := '%honda%';

set linesize 150;
set pagesize 1064;

set echo on;

spool C:\xaxa\DBMS\autohelper\results.txt

select DEALER_NAME, sum
(
  (select count(vin) from newcars where dlr.dealer_id = newcars.dealer_id and date_sold > :start_date) +
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > :start_date) -
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > :start_date)
) as TOTAL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like :make
group by dealer_name
order by TOTAL_SOLD_NEW ASC;

exec :make := '%toyota%';

select DEALER_NAME, sum
(
  (select count(vin) from newcars where dlr.dealer_id = newcars.dealer_id and date_sold > :start_date) +
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > :start_date) -
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > :start_date)
) as TOTAL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like :make
group by dealer_name
order by TOTAL_SOLD_NEW ASC;

exec :make := '%nissan%';

select DEALER_NAME, sum
(
  (select count(vin) from newcars where dlr.dealer_id = newcars.dealer_id and date_sold > :start_date) +
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > :start_date) -
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > :start_date)
) as TOTAL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like :make
group by dealer_name
order by TOTAL_SOLD_NEW ASC;

spool off;