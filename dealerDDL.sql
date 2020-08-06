/*==============================================================*/
/* DDL                                                          */
/*==============================================================*/

/* DEALERWEB DROP */

alter table DEALERWEB
   drop constraint PK_DEALERWEB;
   
alter table DEALERWEB
   drop constraint FK_WEB_PROVIDED_BY;
   
alter table DEALERWEB
   drop constraint FK_HAS_WEBPARAMS;

drop index WEB_PROVIDED_BY_FK;

drop table DEALERWEB cascade constraints;

/*=================*/

/* DEALERWEB CREATE */

create table DEALERWEB 
(
   DEALER_ID                   NUMBER(7)            not null,
   DEALER_NAME                VARCHAR2(40)             not null,
   URL                      VARCHAR2(80)             not null,
   WEB_ENGINE                VARCHAR2(20)             not null,
   MIN_NEW                   NUMBER(3)            not null,
   ZIP                   VARCHAR2(10)            not null,
   MONO_BRAND               CHAR(1)            not null,
   LAST_UPD                  DATE,
   PARAM_ID                VARCHAR2(20)             not null,
   constraint PK_DEALERWEB primary key (DEALER_ID)
);

create index WEB_PROVIDED_BY_FK on DEALERWEB
(
   WEB_ENGINE ASC
);

/*alter table DEALERWEB
   add constraint FK_WEB_PROVIDED_BY foreign key (WEB_ENGINE)
      references WEBENGINE (WEB_ENGINE);*/

alter table DEALERWEB
   add constraint FK_HAS_WEBPARAMS foreign key (PARAM_ID)
      references WEBPARAMS (PARAM_ID);

/*=================*/

/* WEBENGINE DROP */
      
/*alter table WEBENGINE
   drop constraint PK_WEBENGINE;

drop table WEBENGINE cascade constraints;*/

/*=================*/

/* WEBENGINE CREATE */

/* *** DEPRECATED ***  */

/*create table WEBENGINE
(
   WEB_ENGINE                VARCHAR2(20)            not null,
   URL_NEW                VARCHAR2(60)             not null,
   URL_USED                VARCHAR2(60)             not null,
   URL_NEXTPAGE                VARCHAR2(20)             not null,
   TTL_KEY_NEW                VARCHAR2(40)             not null,
   TTL_KEY_USED                VARCHAR2(40)             not null,
   constraint PK_WEBENGINE primary key (WEB_ENGINE)
);*/

/*=================*/

/* WEBPARAMS DROP */
      
alter table WEBPARAMS
   drop constraint PK_WEBPARAMS;

drop table WEBPARAMS cascade constraints;

/*=================*/

/* WEBPARAMS CREATE */

create table WEBPARAMS
(
   PARAM_ID                VARCHAR2(20)            not null,
   URL_NEW                VARCHAR2(60)             not null,
   URL_USED                VARCHAR2(60)             not null,
   URL_NEXTPAGE                VARCHAR2(60)             not null,
   TTL_KEY_NEW                VARCHAR2(60)             not null,
   TTL_KEY_USED                VARCHAR2(60)             not null,
   STARTPAGENEW                   NUMBER(2)            not null,
   PAGEINCNEW                   NUMBER(2)            not null,
   STARTPAGEUSED                   NUMBER(2)            not null,
   PAGEINCUSED                   NUMBER(2)            not null,
   constraint PK_WEBPARAMS primary key (PARAM_ID)
);

/*=================*/

create table NEWCARS
(
    VIN               CHAR(17)            not null,
    DEALER_ID                   NUMBER(7)            not null,
    DATE_ARRIVED                  DATE        not null,
    DATE_SOLD                  DATE,
    SELL_PRICE                   NUMBER(6,2),
    constraint PK_NEWCARS primary key (VIN)
);

alter table NEWCARS
   add constraint FK_NEW_LOCATED_AT foreign key (DEALER_ID)
      references DEALERWEB (DEALER_ID);
      
create index NEW_LOCATED_AT_FK on NEWCARS
(
   DEALER_ID ASC
);

create table USEDCARS
(
    VIN                      CHAR(17)            not null,
    DEALER_ID                   NUMBER(7)            not null,
    DATE_ARRIVED                  DATE        not null,
    DATE_SOLD                  DATE,
    AQUIS_PRICE                   NUMBER(6,2),
    SELL_PRICE                   NUMBER(6,2),
    constraint PK_USEDCARS primary key (VIN, DEALER_ID, DATE_ARRIVED)
    /* A car can be possessed by multiple dealers over the life span. */
    /* It can also return to the dealer who owned it in the past. */
);

alter table USEDCARS
   add constraint FK_USED_LOCATED_AT foreign key (DEALER_ID)
      references DEALERWEB (DEALER_ID);
      
create index USED_LOCATED_AT_FK on USEDCARS
(
   DEALER_ID ASC
);

/*=== END OF USEDCARS ===*/

/* FRANCHISE CREATE */

create table FRANCHISE 
(
   DEALER_ID                NUMBER(7)            not null,
   MANUF                VARCHAR2(30)             not null,
   BRAND                VARCHAR2(30)             not null,
   constraint PK_FRANCHISE primary key (DEALER_ID, BRAND)
)

/* === END OF FRANCHISE === */

/* VEHICLES DROP */
      
alter table VEHICLES
   drop constraint PK_MODELS;

drop table VEHICLES cascade constraints;

/* VEHICLES CREATE */

create table VEHICLES 
(
   VIN                      CHAR(17)            not null,
   MODELYEAR                 CHAR(4)            not null,
   MAKE                VARCHAR2(25)             not null,
   MODEL                VARCHAR2(25)             not null,
   ADDLINFO                VARCHAR2(50)          ,
   COLOR                VARCHAR2(45)             ,
   constraint PK_MODELS primary key (VIN)
);

/* === END OF VEHICLES === */

/* DEALERSWAP CREATE */

create table DEALERSWAP
(
    VIN               CHAR(17)            not null,
    DEALER_ID_FROM     NUMBER(7)            not null,
    DEALER_ID_TO     NUMBER(7)            not null,
    DATE_SWAPPED                  DATE        not null,
    constraint PK_DEALERSWAP primary key (VIN, DATE_SWAPPED)
);

alter table DEALERSWAP
   add constraint FK_TRANSFERED_FROM foreign key (DEALER_ID_FROM)
      references DEALERWEB (DEALER_ID);
      
alter table DEALERSWAP
   add constraint FK_TRANSFERED_TO foreign key (DEALER_ID_TO)
      references DEALERWEB (DEALER_ID);
      
alter table DEALERSWAP
   add constraint CH_DEALERS_DIFF check (DEALER_ID_FROM != DEALER_ID_TO);
      
/* === END OF DEALERSWAP === */

/*==============================================================*/
/* DATA                                                         */
/*==============================================================*/

/*Insert into WEBENGINE Values ('DEALERCOM'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Vehicles matching', 'Vehicles matching');
Insert into WEBENGINE Values ('DEALERFIRE',	'/new-toyota-burnsville-mn',	'/used-cars-burnsville-mn', '?page=', 'New Toyota Burnsville Minnesota', 'Used cars in Burnsville MN');
Insert into WEBENGINE Values ('EBIZAUTOS'	,	'/honda.aspx',	'/used-cars.aspx', '?_page=', 'Hondas Found', 'Vehicles Found');*/

Insert into WEBPARAMS Values ('DCDEFAULT'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DFTB',	'/new-toyota-burnsville-mn',	'/used-cars-burnsville-mn', '?page=', 'New Toyota Burnsville Minnesota', 'Used cars in Burnsville MN', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DFSTCLT',	'/new-toyota-st-cloud-mn',	'/used-cars-st-cloud-mn', '?page=', 'New Toyota St. Cloud Minnesota', 'Used cars in St. Cloud MN', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DFVIGH',	'/new-volkswagen-inver-grove-heights-mn',	'/used-cars-inver-grove-heights-mn', '?page=', 'New Volkswagen Inver Grove Heights Minnesota', 'Pre-Owned cars in Inver Grove Heights MN', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('EARBH'	,	'/honda.aspx',	'/used-cars.aspx', '?_page=', 'Hondas Found', 'Vehicles Found', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DCBH'	,	'/new-inventory/honda-minneapolis.htm',	'/used-inventory/index.htm', '?start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DCIGT'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Items Matching', 'Items Matching', 0, 16, 0, 35);
Insert into WEBPARAMS Values ('DODEFAULT'	,	'/searchnew.aspx?pn=1000',	'/searchused.aspx?pn=1000', '&pt=', 'Vehicles)', 'Vehicles)', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DIDEFAULT'	,	'/new-vehicles/',	'/used-vehicles/', 'NA', 'Matching Vehicles', 'Matching Vehicles', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DFHEC',	'/new-cars-eau-claire-wi',	'/used-cars-eau-claire-wi', '?page=', 'New cars in Eau Claire WI', 'Pre-Owned cars in Eau Claire WI', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DCLKB'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Vehicles matching', 'Items Matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DCLKIG'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Items Matching', 'Items Matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DCMS'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Vehicles matching', 'Vehicles matching', 0, 35, 0, 35);
Insert into WEBPARAMS Values ('DCLBH'	,	'/new-inventory/index.htm?accountId=lutherbrookdalehonda',	'/used-inventory/index.htm?accountId=lutherbrookdalehonda', '&start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DCLWVW'	,	'/new-inventory/index.htm',	'/used-inventory/index.htm', '?start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 35);
Insert into WEBPARAMS Values ('AWDEFAULT'	,	'/inventory/New/',	'/inventory/Used/', '?page=', 'Vehicles Found', 'Vehicles Found', 1, 1, 1, 1);
Insert into WEBPARAMS Values ('DCDWNB'	,	'/new-inventory/index.htm?',	'/used-inventory/index.htm?accountId=walsernissan', '&start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DCDWNCR'	,	'/new-inventory/index.htm?',	'/used-inventory/index.htm?accountId=coonrapidsnissan', '&start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 16);
Insert into WEBPARAMS Values ('DCDWNW'	,	'/new-inventory/index.htm?',	'/used-inventory/index.htm?accountId=walserwayzatanissan', '&start=', 'Vehicles matching', 'Vehicles matching', 0, 16, 0, 16);

Insert into DEALERWEB Values (1	,	'Walser Honda',	'http://www.walserhonda.com', 'DEALERCOM', 450, '55306', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (2	,	'Burnsville Toyota',	'https://www.burnsvilletoyota.com', 'DEALERFIRE', 380, '55306', 'Y', null, 'DFTB');
Insert into DEALERWEB Values (3	,	'Richfield Bloomington Honda',	'http://www.richfieldbloomingtonhonda.com', 'EBIZAUTOS', 380, '55423', 'Y', null, 'EARBH');
Insert into DEALERWEB Values (4	,	'Hopkins Honda',	'https://www.lutherhopkinshonda.com', 'DEALERCOM', 380, '55343', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (5	,	'Walser Toyota',	'http://www.walsertoyota.com', 'DEALERCOM', 400, '55437', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (6	,	'VW Inver Grove',	'http://www.vwinvergrove.com', 'DEALERFIRE', 80, '55077', 'Y', null, 'DFVIGH');
Insert into DEALERWEB Values (7	,	'Buerkle Honda',	'https://www.buerklehonda.com', 'AUTOWEBING', 160, '55110', 'Y', null, 'AWDEFAULT');
Insert into DEALERWEB Values (8	,	'Luther Brookdale Toyota',	'https://www.lutherbrookdaletoyota.com', 'DEALERCOM', 420, '55429', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (9	,	'Inver Grove Toyota',	'http://www.invergrovetoyota.com', 'DEALERCOM', 300, '55077', 'Y', null, 'DCIGT');
Insert into DEALERWEB Values (10,	'Rudy Luther Toyota',	'https://www.rudyluthertoyota.com', 'DEALERCOM', 600, '55426', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (11,	'Luther Brookdale Honda',	'http://www.lutherauto.com', 'DEALERCOM', 450, '55429', 'Y', null, 'DCLBH');
Insert into DEALERWEB Values (12,	'Maplewood Toyota',	'https://www.maplewoodtoyota.com', 'DEALERON', 480, '55109', 'Y', null, 'DODEFAULT');
Insert into DEALERWEB Values (13,	'Luther St Cloud Honda',	'https://www.lutherhondaofstcloud.com', 'DEALERCOM', 60, '56387', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (14,	'Mills Honda',	'https://www.millshonda.com', 'DEALERON', 60, '56425', 'Y', null, 'DODEFAULT');
Insert into DEALERWEB Values (15,	'Carlson Toyota',	'http://www.carlsontoyota.com', 'DEALERCOM', 260, '55448', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (16,	'Inver Grove Honda',	'http://www.invergrovehonda.com', 'DEALERINSPIRE', 500, '55077', 'Y', null, 'DIDEFAULT');
Insert into DEALERWEB Values (17,	'Heintz Toyota',	'http://www.heintztoyota.com', 'DEALERCOM', 60, '56001', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (18,	'St Cloud Toyota',	'https://www.stcloudtoyota.com', 'DEALERFIRE', 100, '56387', 'Y', null, 'DFSTCLT');
Insert into DEALERWEB Values (19,	'Luther Mankato Honda',	'https://www.luthermankatohonda.com', 'DEALERCOM', 100, '56001', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (20,	'Ken Vance Honda',	'http://www.kenvancehonda.com', 'DEALERFIRE', 70, '54701', 'Y', null, 'DFHEC');
Insert into DEALERWEB Values (21,	'Luther Kia Bloomington',	'https://www.lutherkiaofbloomington.com', 'DEALERCOM', 100, '55431', 'Y', null, 'DCLKB');
Insert into DEALERWEB Values (22,	'Luther Kia Inver Grove',	'https://www.lutherkiamn.com', 'DEALERCOM', 100, '55077', 'Y', null, 'DCLKIG');
Insert into DEALERWEB Values (23,	'Tom Kadlec Honda',	'https://www.tomkadlec.com', 'DEALERON', 60, '55901', 'Y', null, 'DODEFAULT');
Insert into DEALERWEB Values (24,	'Morries Minnetonka Subaru',	'http://www.minnetonkasubaru.com', 'DEALERCOM', 100, '55305', 'Y', null, 'DCMS');
Insert into DEALERWEB Values (25,	'Walser Subaru',	'http://www.walsersubaru.com', 'DEALERCOM', 100, '55337', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (26,	'Morries Brooklyn Park Subaru',	'http://www.brooklynparksubaru.com', 'DEALERCOM', 100, '55445', 'Y', null, 'DCMS');
Insert into DEALERWEB Values (27,	'Luther Bloomington Subaru',	'https://www.bloomingtonsubaru.com', 'DEALERCOM', 100, '55420', 'Y', null, 'DCMS');
Insert into DEALERWEB Values (28,	'Luther White Bear Subaru',	'https://www.whitebearsubaru.com', 'DEALERCOM', 100, '55110', 'Y', null, 'DCMS');
Insert into DEALERWEB Values (29,	'Luther Burnsville VW',	'https://www.burnsvillevw.com', 'DEALERCOM', 100, '55306', 'Y', null, 'DCMS');
Insert into DEALERWEB Values (30,	'Luther Westside VW',	'https://www.westsidevw.com', 'DEALERCOM', 200, '55306', 'Y', null, 'DCLWVW');
Insert into DEALERWEB Values (31,	'Luther Brookdale VW',	'https://www.lutherbrookdalevw.com', 'DEALERCOM', 100, '55429', 'Y', null, 'DCMS');
Insert into DEALERWEB Values (32,	'Schmelz VW',	'https://www.schmelzvw.com', 'DEALERON', 80, '55109', 'Y', null, 'DODEFAULT');
Insert into DEALERWEB Values (33,	'Walser Nissan',	'http://www.walsernissanburnsville.com', 'DEALERCOM', 250, '55306', 'Y', null, 'DCDWNB');
Insert into DEALERWEB Values (34,	'Eden Prairie Nissan',	'https://www.edenprairienissan.com', 'DEALERINSPIRE', 50, '55344', 'Y', null, 'DIDEFAULT');
Insert into DEALERWEB Values (35,	'Wayzata Nissan',	'http://www.walsernissanwayzata.com', 'DEALERCOM', 350, '55391', 'Y', null, 'DCDWNW');
Insert into DEALERWEB Values (36,	'Luther Nissan',	'https://www.luthernissan.com', 'DEALERCOM', 150, '55077', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (37,	'Kline Nissan',	'http://www.klinenissan.com', 'DEALERCOM', 150, '55109', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (38,	'Morries Nissan',	'https://www.morriesbrooklynparknissan.com', 'DEALERINSPIRE', 150, '55445', 'Y', null, 'DIDEFAULT');
Insert into DEALERWEB Values (39,	'Coon Rapids Nissan',	'http://www.coonrapidsnissan.com', 'DEALERCOM', 150, '55448', 'Y', null, 'DCDWNCR');
Insert into DEALERWEB Values (40,	'Walser Mazda',	'http://www.walser-mazda.com', 'DEALERCOM', 250, '55306', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (41,	'Morries IG Mazda',	'https://www.morriesinvergrovemazda.com', 'DEALERINSPIRE', 150, '55077', 'Y', null, 'DIDEFAULT');
Insert into DEALERWEB Values (42,	'Morries Minnetonka Mazda',	'https://www.morriesmazda.com', 'DEALERINSPIRE', 250, '55305', 'Y', null, 'DIDEFAULT');
Insert into DEALERWEB Values (43,	'Luther Brookdale Mazda',	'https://www.luthermazda.com', 'DEALERCOM', 250, '55429', 'Y', null, 'DCDEFAULT');
Insert into DEALERWEB Values (44,	'Polar Mazda',	'http://www.polarmazda.com', 'DEALERCOM', 250, '55110', 'Y', null, 'DCDEFAULT');

Insert into NEWCARS Values ('1HGCT1B88GA003361', 1,	TO_DATE('2017/02/05', 'yyyy/mm/dd'), null, null);
Insert into NEWCARS Values ('1HGCT1B88GA003111', 1,	TO_DATE('2017/01/05', 'yyyy/mm/dd'), TO_DATE('2017/02/05', 'yyyy/mm/dd'), null);
Insert into NEWCARS Values ('1HGCT1B88GA003222', 1,	TO_DATE('2017/02/05', 'yyyy/mm/dd'), null, null);

/*==============================================================*/
/* QUERIES                                                      */
/*==============================================================*/

set linesize 380;
set pagesize 1064;

set echo on;

spool C:\xaxa\DBMS\autohelper\results.txt

select dealer_id, url, min_new, web_engine, url_new, url_used, url_nextpage, ttl_key_new, ttl_key_used, startpagenew, pageincnew, startpageused, pageinused
from DEALERWEB left outer join WEBPARAMS
on lower(WEBPARAMS.param_id) = lower(DEALERWEB.param_id);

spool off;

UPDATE WEBENGINE
SET URL_NEXTPAGE='?_page='
WHERE upper(WEBENGINE.web_engine)='EBIZAUTOS';

select VIN from NEWCARS where dealer_id = 1 and DATE_SOLD is null;

/* Used vehicle return */

delete from usedcars where vin = 'KM8SMDHF5FU099621' and date_sold is null;
update usedcars set date_sold = null where vin = 'KM8SMDHF5FU099621';

delete from usedcars where vin = '5FRYD4H43EB011815' and date_sold = '28-feb-17';

/* New to Used return */

delete from newcars where vin = '5J6RM4H70GL030669';
update usedcars set date_arrived = '28-feb-17' where vin = '5J6RM4H70GL030669';

/* Number of units sold */

select DEALER_NAME, count (vin) as SOLD_NEW
from newcars left outer join dealerweb
on newcars.dealer_id = dealerweb.dealer_id
where date_sold > '01-may-17' and lower(dealer_name) like '%honda%'
group by DEALER_NAME
order by count(vin) ASC;

select DEALER_NAME, count (vin) as SALE_MINUS
from dealerswap left outer join dealerweb
on dealerswap.dealer_id_from = dealerweb.dealer_id
where date_swapped > '01-apr-17' and lower(dealer_name) like '%honda%'
group by DEALER_NAME;

select DEALER_NAME, count (vin) as SALE_PLUS
from dealerswap left outer join dealerweb
on dealerswap.dealer_id_to = dealerweb.dealer_id
where date_swapped > '01-apr-17' and lower(dealer_name) like '%honda%'
group by DEALER_NAME;

/* TOTAL SOLD NEW with dealerswap */

select DEALER_NAME, sum
(
  (select count(vin) from newcars where dlr.dealer_id = newcars.dealer_id and date_sold > '01-may-17') +
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > '01-may-17') -
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > '01-may-17')
) as TOTAL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like '%honda%'
group by dealer_name
order by TOTAL_SOLD_NEW ASC;

/* same with var */

var start_date varchar2(9);
exec :start_date := '01-sep-17';

select DEALER_NAME, sum
(
  (select count(vin) from newcars where dlr.dealer_id = newcars.dealer_id and date_sold > :start_date) +
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > :start_date) -
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > :start_date)
) as TOTAL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like '%honda%'
group by dealer_name
order by TOTAL_SOLD_NEW ASC;

/* date range */

select DEALER_NAME, sum
(
  (select count(vin) from newcars where dlr.dealer_id = newcars.dealer_id and date_sold > '01-may-17' and date_sold < '21-may-17') +
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > '01-may-17' and date_swapped < '21-may-17') -
  (select count(vin) from dealerswap where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > '01-may-17' and date_swapped < '21-may-17')
) as TOTAL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like '%honda%'
group by dealer_name
order by TOTAL_SOLD_NEW ASC;

/* Model SOLD NEW with dealerswap */

var car varchar2(20);
exec :car := 'odyssey';

select DEALER_NAME, sum
(
  (select count(newcars.vin) from newcars, vehicles
   where dlr.dealer_id = newcars.dealer_id and newcars.vin = vehicles.vin and date_sold > '01-may-17' and lower(model) = :car) +
  (select count(dealerswap.vin) from dealerswap, vehicles
   where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > '01-may-17' and dealerswap.vin = vehicles.vin and lower(model) = :car) -
  (select count(dealerswap.vin) from dealerswap, vehicles
   where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > '01-may-17' and dealerswap.vin = vehicles.vin and lower(model) = :car)
) as MODEL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like '%honda%'
group by dealer_name
order by MODEL_SOLD_NEW ASC;

exec :car := 'fit';

select sum
(
  (select count(newcars.vin) from newcars, vehicles
   where dlr.dealer_id = newcars.dealer_id and newcars.vin = vehicles.vin and date_sold > '14-may-17' and lower(model) = :car) +
  (select count(dealerswap.vin) from dealerswap, vehicles
   where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > '14-may-17' and dealerswap.vin = vehicles.vin and lower(model) = :car) -
  (select count(dealerswap.vin) from dealerswap, vehicles
   where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > '14-may-17' and dealerswap.vin = vehicles.vin and lower(model) = :car)
) as MODEL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like '%honda%';

select DEALER_NAME, sum
(
  (select count(newcars.vin) from newcars, vehicles
   where dlr.dealer_id = newcars.dealer_id and newcars.vin = vehicles.vin and date_sold > '01-apr-17' and date_sold < '03-may-17' and lower(model) = 'odyssey') +
  (select count(dealerswap.vin) from dealerswap, vehicles
   where dlr.dealer_id = dealerswap.dealer_id_to and date_swapped > '01-apr-17' and date_swapped < '03-may-17' and dealerswap.vin = vehicles.vin and lower(model) = 'odyssey') -
  (select count(dealerswap.vin) from dealerswap, vehicles
   where dlr.dealer_id = dealerswap.dealer_id_from and date_swapped > '01-apr-17' and date_swapped < '03-may-17' and dealerswap.vin = vehicles.vin and lower(model) = 'odyssey')
) as MODEL_SOLD_NEW
from dealerweb dlr
where lower(dealer_name) like '%honda%'
group by dealer_name
order by MODEL_SOLD_NEW ASC;

/* Current stock new */
set echo on;

spool C:\xaxa\DBMS\autohelper\results.txt

select dealer_name, date_arrived, newcars.vin, modelyear, ADDLINFO from newcars, vehicles, dealerweb
where lower(newcars.vin) = lower(vehicles.vin) and lower(model) = 'odyssey' and dealerweb.dealer_id = newcars.dealer_id and date_sold is null and modelyear = '2016'
order by date_arrived ASC;

spool off;

/* Current stock new with dealerswap */
set linesize 380;
set pagesize 64;

set echo on;

spool C:\xaxa\DBMS\autohelper\results.txt

select date_swapped, dealer_name, date_arrived, new_vin, modelyear, ADDLINFO from
(select dealer_name, newcars.dealer_id as d_id, date_arrived, newcars.vin as new_vin, modelyear, ADDLINFO from newcars, vehicles, dealerweb
where lower(newcars.vin) = lower(vehicles.vin) and lower(model) = 'pilot' and dealerweb.dealer_id = newcars.dealer_id and date_sold is null /*and modelyear = '2017'*/) all_cars
left outer join dealerswap
on lower(all_cars.new_vin) = lower(dealerswap.vin) and all_cars.d_id = dealerswap.dealer_id_to
order by date_arrived ASC;

spool off;

/* Current stock new by model*/
set echo on;

spool C:\xaxa\DBMS\autohelper\results.txt

select dealer_name, count(newcars.vin) from newcars, vehicles, dealerweb
where lower(newcars.vin) = lower(vehicles.vin) and lower(model) = 'pilot' and dealerweb.dealer_id = newcars.dealer_id and date_sold is null-- and modelyear = '2016'
group by dealer_name
order by count(newcars.vin) ASC;

spool off;

/* date range */
select dealer_name, count(newcars.vin) from newcars, vehicles, dealerweb
where lower(newcars.vin) = lower(vehicles.vin) and lower(model) = 'odyssey' and dealerweb.dealer_id = newcars.dealer_id and date_arrived < '20-may-17' and
((date_sold is null) or (date_sold > '21-may-17'))
group by dealer_name
order by count(newcars.vin) ASC;


/* Current stock used */
select dealer_name, date_arrived, usedcars.vin, modelyear, ADDLINFO from usedcars, vehicles, dealerweb
where lower(usedcars.vin) = lower(vehicles.vin) and lower(model) = 'odyssey' and dealerweb.dealer_id = usedcars.dealer_id and date_sold is null and modelyear = '2016'
order by date_arrived ASC;

/*==============================================================*/
/* SAND BOX                                                     */
/*==============================================================*/

create table SBNEWCARS
(
    VIN               CHAR(17)            not null,
    DEALER_ID                   NUMBER(7)            not null,
    DATE_ARRIVED                  DATE        not null,
    DATE_SOLD                  DATE,
    SELL_PRICE                   NUMBER(6,2),
    constraint PK_SBNEWCARS primary key (VIN)
);

Insert into SBNEWCARS Values ('1HGCT1B88GA003361', 1,	TO_DATE('2017/02/05', 'yyyy/mm/dd'), null, null);
Insert into SBNEWCARS Values ('1HGCT1B88GA003111', 1,	TO_DATE('2017/01/05', 'yyyy/mm/dd'), TO_DATE('2017/02/05', 'yyyy/mm/dd'), null);
Insert into SBNEWCARS Values ('1HGCT1B88GA003222', 1,	TO_DATE('2017/02/05', 'yyyy/mm/dd'), null, null);

Insert into SBUSEDCARS Values ('1GCVKREC4FZ298516', 9,	TO_DATE('2017/09/30', 'yyyy/mm/dd'), null, null, null);
Insert into SBUSEDCARS Values ('1GCVKREC4FZ298516', 7,	TO_DATE('2017/09/12', 'yyyy/mm/dd'), TO_DATE('2017/09/28', 'yyyy/mm/dd'), null, null);
Insert into SBUSEDCARS Values ('1GCVKREC4FZ298516', 5,	TO_DATE('2017/08/1', 'yyyy/mm/dd'), TO_DATE('2017/08/19', 'yyyy/mm/dd'), null, null);

create table SBVEHICLES 
(
   VIN                      CHAR(17)            not null,
   MODELYEAR                 CHAR(4)            not null,
   MAKE                VARCHAR2(25)             not null,
   MODEL                VARCHAR2(25)             not null,
   ADDLINFO                VARCHAR2(50)          ,
   COLOR                VARCHAR2(35)             ,
   constraint PK_SBMODELS primary key (VIN)
);

create table SBUSEDCARS
(
    VIN                      CHAR(17)            not null,
    DEALER_ID                   NUMBER(7)            not null,
    DATE_ARRIVED                  DATE        not null,
    DATE_SOLD                  DATE,
    AQUIS_PRICE                   NUMBER(6,2),
    SELL_PRICE                   NUMBER(6,2),
    constraint PK_SBUSEDCARS primary key (VIN, DEALER_ID, DATE_ARRIVED)
    /* A car can be possessed by multiple dealers over the life span. */
    /* It can also return to the dealer who owned it in the past. */
);

create table SBDEALERSWAP
(
    VIN               CHAR(17)            not null,
    DEALER_ID_FROM     NUMBER(7)            not null,
    DEALER_ID_TO     NUMBER(7)            not null,
    DATE_SWAPPED                  DATE        not null,
    constraint PK_SBDEALERSWAP primary key (VIN, DATE_SWAPPED)
);

Insert into SBVEHICLES Values ('1HGCT1B88GA003361', '2017',	'Honda', 'Accord', null);
Insert into SBVEHICLES Values ('1HGCT1B88GA003111', '2017',	'Honda', 'Accord', null);
Insert into SBVEHICLES Values ('1HGCT1B88GA003222', '2017',	'Honda', 'Accord', null);
--Insert into SBVEHICLES Values ('1HGCT1B88GA003244', '2017',	'Honda', 'Accord', null);--duplicate vin test

delete from sbnewcars;
delete from sbusedcars;
delete from sbvehicles;
delete from sbdealerswap;

alter table webparams
add    (STARTPAGENEW                   NUMBER(2)            not null,
   PAGEINCNEW                   NUMBER(2)            not null,
   STARTPAGEUSED                   NUMBER(2)            not null,
   PAGEINCUSED                   NUMBER(2)            not null);