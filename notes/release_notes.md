VERSION 3.01.2.17.13 - 06-03-2024
================================

1. Added CLEAR on Giftcard ROA

VERSION 3.01.2.17.10 - 01-03-2024
================================

1. Fixed SSL

VERSION 3.01.2.17.9 - 29-02-2024
================================

1. Added SSCO handling to Ogloba

To allow for direct serial insertion from SSCO , the tender must be configured properly in P_REGTND.DAT:

    17:S:Spinneys GC         :08:1:00:00:90000025:00000000:00000025:00000000:00000000:00000000:00000000:00000000:00000000:00000000
                              ^^
                              The flag must be set to 08

New EVENT.TBL is needed

2. name of giftCard.properties has been changed to gift-card-plugins.properties

VERSION 3.01.2.17.8 - 27-02-2024
================================

1. Fixes on Ogloba
2. Check GC code to prohibit different chains GC in payment

VERSION 3.01.2.17.7 - 26-02-2024
================================

1. Fixes on Ogloba

Added messages mnemonics (P_REGMOD.DAT):

    /* MNEMO[115] */ "GEN CODE    ",
    /* MNEMO[116] */ "SERIAL      ",

    /* MENUS[139] */"GIFT CARD TRANSACT. ",

The following parameter will allow the insertion of serial number through keyboard (only in payment)

ogloba.properties
-----------------

    keyboard-enabled = true


VERSION 3.01.2.17.6 - 26-02-2024
================================

1. Fixes on Ogloba

Amount on min and max must be expressed in cents

ogloba.properties
-----------------

    reload.max.amount=5000
    reload.min.amount=1000

VERSION 3.01.2.17.5 - 25-02-2024
================================

Ogloba as a ROA - Added a new button with code:

    0xAE - Gift Card ROA

This button must be added in P_REGKEY.DAT to start GC ROA transaction
A dynakey menu will be shown with the following texts:

        /* MENUS[135] */"GIFT CARD SALE      ",
        /* MENUS[136] */"GIFT CARD TOPUUP    ",
        /* MENUS[137] */" PAYMENT            ",
        /* MENUS[138] */"  CANCEL            ",

Text can be customizable through P_REGMOD.DAT

Two new parameters have been added to ogloba.properties to define 
ROA totalizers (these must be present in S_PLUACC.DAT):

    059402030010000000    GC RELAOD       S1xxxxxxxx
    059501030010000000    GC SALE         S0xxxxxxxx

Two parameters will define max and min amount to be loaded on GC.

ogloba.properties
-----------------

    account.sale=0595
    account.topup=0594
    reload.max.amount=50
    reload.min.amount=10

VERSION 3.01.2.17.4 - 22-02-2024
================================

1. Fixed Instashop.txt corruption
2. Fixed cancel on Ecom
3. Print receipt also if not a basket
4. DP disabled on a new parameter
5. Fixed VAT on return QR Code

ecommerce.properties
----------------------

    special-sales.enabled=true
    canApplyDsc=false

VERSION 3.01.2.17.3 - 15-02-2024
================================

1. Fixed Credimax integration startup

VERSION 3.01.2.17.1 - 13-02-2024
================================

1. Fixed integration issue
2. Fixed return on VAT charges
3. Added default membership level for AYM and Philoshopic

philoshopic.properties
----------------------

    branch.code=10

loyalty.properties
----------------------

    branch.code=10

VERSION 3.01.2.17.0 - 12-02-2024
================================

1. Merged Credimax

VERSION 3.01.2.16.12 - 19-01-2024
================================

1. Ogloba new functions: refund and tender type depending on card mask
2. Surcharge fixes
3. Added customer category 1 on all Philoshopic customers

ogloba.properties
-----------------

    username=SPINNEYS01  
    password=3Zj8mlA1nNk87iuGQOWTHlvq6t21066120kjgs0HN89Nx6MqTV  
    endPoint=https://demo.ogloba.com/gc-restful-gateway/giftCardService  
    protocol=https  
    timeout=180000  
    merchantId=SPINNEYS01  
    enabled=true  
    
    #accountType list  
    accountType.25=888[0-9]+  
    accountType.32=655[0-9]+  

    #temporary reconciliation file path
    reconciliationFile.path=c:/gd90/data/
    reconciliationFile.name.mask=gc_drf_merchantId_YYYYMMDD.json
    reconciliationFile.firstLine.format=businessDate,merchantId
    #Max Amount for Reload a GC
    reload.max.amount=50
    #Print options
    print-gift-cards.same-receipt=true
    print-gift-cards.new-receipt=true
    printSerialEnabled = true
    #Max Amount for Refund a GC
    refund.max.amount=50

already described before in version 16.0.
Added the following:

    accountType.XX=888[0-9]+

Where all cards starting with 888 will be mapped to tender XX.

And the following:

    refund.max.amount=YY

to limit the amount refunded on a Gif Card to YY AED (positive full value, no coins)

VERSION 3.01.2.16.9 - 17-01-2024
================================

1. fix

VERSION 3.01.2.16.8 - 12-01-2024
================================

1. added 'i' IDC record after 'D' for surcharge
2. handled surcharge also in QRcode items
3. Fixed double TLOG for Philoshopic

VERSION 3.01.2.16.7 - 07-01-2024
================================

1. Fix VAT spreading in e-commerce surcharge
2. Merged int main

VERSION 3.01.2.16.6 - 01-01-2024
================================

1. Fix multiple void against single sale

VERSION 3.01.2.16.0 - 21-12-2023
================================

SUMMARY
-------
This release contains the following new functionalities:

1. Ogloba Gift Card integration
2. Cashier autologon
3. Charge items for E-commerce
4. E-commerce request by providerID

The following fixes have been applied:

1. Multitender on E-commerce
---

[1] OGLOBA Gift Cards
--------------------
See document in doc directory of release package.

The following configurations must be applied:

ogloba.properties
-----------------
This file contains all the configuration specific for Ogloba

    username=SPINNEYS01
    password=3Zj8mlA1nNk87iuGQOWTHlvq6t21066120kjgs0HN89Nx6MqTV
    endPoint=https://demo.ogloba.com/gc-restful-gateway/giftCardService
    protocol=https
    timeout=180000
    merchantId=SPINNEYS01
    enabled=true
    #accountType list
    accountType.01=GIFTCARD;false
    #temporary reconciliation file path
    reconciliationFile.path=c:/gd90/data/
    reconciliationFile.name.mask=gc_drf_merchantId_YYYYMMDD.json
    reconciliationFile.firstLine.format=businessDate,merchantId
    #Max Amount for Reload a GC
    reload.max.amount=50

giftCard.properties
-------------------

    OglobaPlugin = true  
    GdPsh = false  

This will enable Ogloba gift cards

errorCodes.properties
---------------------
This file must be updated with the error codes used for Ogloba (see **errorCodes.properties.UPD**)

    oglobaPlugin.error.85=Card had been activated
    oglobaPlugin.error.104=Can't reload a non-activated card
    oglobaPlugin.error.85=Amount exceeds max

P_REGTND.DAT
------------

A line for an Ogloba tender will be like the following (S tender type):

    17:S:Ogloba              :00:1:00:00:90000025:00000000:00000025:00000000:00000000:00000000:00000000:00000000:00000000:00000000

P_REGPAR.DAT & P_REGKEY.DAT
---------------------------
Configure the tender in CMNY record and FKEY record

[2]  Cashier Autologon
----------------------
To enable and configure cashier autologon, use the following:

auto-cmd.properties
-------------------

    enabled = true
    operator = 310

- `enabled` will enable functionality
- With `operator` the cashier to be logged on will be defined (password will not be asked)

[3]  Charge items for E-commerce
--------------------------------
This functionality will allow to handle specific items in the E-commerce basket as surcharges on the 
basket sale instead of normal items.

ecommerce.properties
--------------------
In this file the following parameters must be added:

    special.Amazon = 5;0411
    special.Instashop = 6;0412
    special.Spinneys = 7;0413

Every line starting with `special` will define a different special item to be treated as a surcharge.
The value of each line is made of two blocks separated by a semicolon ';'. The first one is the item code,
the second one is the accumulator in S_REG file. The extension in the key (Amazon, Instashop, ... is only 
for identification purposes). Therefore, for example, the following:

    special.Amazon = 5;0411

says that whenever item 5 is present in the e-commerce basket, it will be treated as a surcharge to be totalized
on S_REG record 0411 (that must be present).

S_REGXXX.ORG
------------
This is an example of additional lines in S_REG:

    0410:E-COMMERCE  :00:0000:00+000000+0000000+0000000000
    0411:Insta Charge:1F:0000:00+000000+0000000+0000000000
    0412:Spinn Charge:1F:0000:00+000000+0000000+0000000000
    0413:Amazn Charge:1F:0000:00+000000+0000000+0000000000
    0419:NET SALES   :11:0000:00+000000+0000000+0000000000

[4] E-commerce request by provider ID
-------------------------------------

ecommerce.properties
--------------------
This functionality is enabled adding the following parameter.

    provider-id = Amazon

The value is the one that will be sent to ECI to retrieve only the baskets for that provider. 
New ECI 2.0.4 must be used.

VERSION 3.01.2.15.12 - 14-12-2023
================================
1. Fix B2B Exception
2. Difference in lane number for Zatca and receipt B2B
3. Return GC

philoshopic.properties
----------------------

item-basket.enable=true
topoup-on-return.enable=true

---

VERSION 3.01.2.15.11 - 13-10-2023
================================
1. Fix Tamara on thresholds

---

VERSION 3.01.2.15.10 - 12-10-2023
================================
1. Fix Tamara on thresholds

---

VERSION 3.01.2.15.9 - 04-10-2023
================================
1. Fix gif card

---

VERSION 3.01.2.15.8 - 04-10-2023
================================
1. Fix reward redemption

---

VERSION 3.01.2.15.7 - 03-10-2023
================================
1. Fix reward redemption

---

VERSION 3.01.2.15.6 - 02-10-2023
================================
1. Fix for automatic payment in Philoshopic environment

---
VERSION 3.01.2.15.5 - 29-09-2023
================================
1. Implementation of cancelReward for AYM loyalty
2. Fix for automatic payment in Philoshopic environment

---

VERSION 3.01.2.15.4 - 28-09-2023
================================
1. Updated Philobroker EFT integration

---

VERSION 3.01.2.15.3 - 27-09-2023
================================
1. Tamara checks on transaction amount.

Configuration:

auto-sale.properties
----------------------
enabled = true

tender-items.7=;112;113;114;115;116;117;118;119;120;121;122;123;124;125;126;127;128;129;130;131;132
tender-thresholds.7=0;10000;15000;20000;25000;30000;40000;50000;65000;80000;100000;140000;180000;220000;250000;300000;350000;400000;450000;500000;550000;9999900
tender-auto-increment.7=true


tender-items.7 is a list of items automatically sold for tender 7 depending on tender-thresholds. For example if tender amount is 120,00 SAR, it will be in the second threshold (over 10000, under 15000), therefore item 112 will be sold (note that first threshold has empty item.

tender-auto-increment set to true means that paying 120,00 SAR will automatically be changed to 128,00 (being 8,00 the price of item 112).

all the items must be configured accordingly (if they are not in PLU, the automatic sale will fail)

to disable all set enabled to false.

---

VERSION 3.01.2.15.2 - 21-09-2023
================================
1. Fixed the vat field for B2B invoices with discounts

---

VERSION 3.01.2.15.1 - 20-09-2023
================================
1. Fixed the MM in invoices. Added spreading of discount among participating items

---

VERSION 3.01.2.15.0 - 31-08-2023
================================
1. First release of Philobroker EFT integration

EVENT.TBL needs to be updated

Configuration:

P_REGTND.DAT
------------

10:Z:SPAN                :00:0:00:  :00000001:00000000:00000001:00000000:00000000:99999900:00000000:00000000:00000000:00000001
11:Z:VISA CARD           :00:0:00:00:00000001:00000000:00000001:00000000:00000000:99999999:00000000:00000000:00000000:00000001
12:Z:MASTER CARD         :00:0:00:00:00000001:00000000:00000001:00000000:00000000:99999999:00000000:00000000:00000000:00000001
13:Z:AMEXCO              :00:0:00:00:00000001:00000000:00000001:00000000:00000000:99999999:00000000:00000000:00000000:00000001

P_REGPAR.DAT
------------

EFTZ0:01500:10:COM18:19200 :0:8:1:1:0000000000
EFTZ1:1111000000000000000011110000000000000000

eft.properties
--------------

url=http://localhost
merchant=merchant-test
currency-code=978
id=default

eftPlugins.properties
---------------------

ToneTagEftPlugin=false
EyePayEftPlugin=false
AlshayaEftPlugin=false
MarshallEftPlugin=false
GeideaEftPlugin=false
NewGeideaEftPlugin=false
PhilobrokerEftPlugin=true

eftTenders.properties
---------------------

Properly configure tenders depending on the response received from terminal.

---

VERSION 3.01.2.14.08 - 22-08-2023
================================
1. Added please wait DN

---

VERSION 3.01.2.14.07 - 21-08-2023
================================
1. Fixed AdditionalInfo data needed

---

VERSION 3.01.2.14.05 - 08-08-2023
=================================
1. Fixed dataneeded on clear

---

VERSION 3.01.2.14.04 - 07-08-2023
=================================
1. Added TenderException for SSCO on readSerial32 with no input

---

VERSION 3.01.2.14.03 - 04-08-2023
=================================
1. Removed Clear DN being sent whenever a Back button is pressed in Dataneeded

---

VERSION 3.01.2.14.02 - 03-08-2023
=================================
1. Fix for cancel being pressed on SSCO during serial number (or phone number) entering

---

VERSION 3.01.2.14.01 - 03-08-2023
=================================
1. Tamimi EFT integration Work in progress

2. Additional TenderTypes for SSCO:

AlRajhi
Manafith
Qitaf

3. Changed the printing of invoice message for B2B. It is printed even if SAP is offline

4. Added B2B Invoice and Simplified invoice to the additional header logic.

Additional Header Logic
-----------------------

It works ONLY if postponed printing is enabled (see ARS documentation). P_REGPAR.DAT line COPT third byte couple:

COPT0:000050000000101025032C001102002001000044
          ^
          must have 1 flag enabled (1 or 3 or 5 or 7)

Header lines are configured in P_REGTXT.DAT record HDRxy, where x is the type of header and y is a sequential from 0 to 9

Possible values of x are:

1 - Ecommerce
2 - Simplified Invoice
3 - B2B Invoice

Therefore configuration in P_REGTXT.DAT is as following:

Simplified Invoice:
------------------
HDR20:Simplified Invoice                      
HDR21:Simplified Invoice                      
HDR22:...

till HDR29. If a record is not present it is not printed

B2B Invoice:
-----------
HDR30:B2B Invoice
HDR31:...

till HDR39. If a record is not present it is not printed

---

VERSION 3.01.2.13.11 - 24-07-2023
================================
1. Added Tamimi B2B invoice serialization
    
    files are added to gd90\b2b\fail directory if there has been some error in SAP communication
    files are added to gd90\b2b\success otherwise

    filename has the following format:

   invoice_202307241060162633.json
           yyyymmdd store reg trans

    directories are created if not present

2. Added IDC also for offline invoices

success:
0106:010:230724:153148:2633:010:y:109:0000:                               1234
                                   ^
                                   zero

error:
0106:010:230724:152653:2631:008:y:119:0000:                               1234
                                   ^
                                   one
---

VERSION 3.01.2.13.10 - 20-07-2023
================================
1. Added dataneeded also on QR code generation error

---

VERSION 3.01.2.13.9 - 18-07-2023
================================
1. Modified Zatca dataneeded (see below)
2. Added field 10 in protocol with Philoshopic to retrieve money equivalent to points (API 2.2)
3. Added vat field on items for B2B invoices

dataneeded.properties:

######################################################################
# Zatca issue message Data Needed
######################################################################
ZatcaMessage.Type=Alert
ZatcaMessage.Id=2
ZatcaMessage.Mode=0
ZatcaMessage.TopCaption.1=Zatca Issue
ZatcaMessage.SummaryInstruction.1=Zatca Issue
ZatcaMessage.EnableScanner=0
ZatcaMessage.HideHelp=1
ZatcaMessage.HideGoBack=1
ZatcaMessage.HideTotal=0
ZatcaMessage.TableName=POSMessages

---

VERSION 3.01.2.13.8 - 16-07-2023
================================
Fixes on Zatca loading of properties
Added a Data needed to show on SSCO the Zatca status. 

1. dataneeded.properties

######################################################################
# Zatca issue message Data Needed
######################################################################
ZatcaMessage.Type=Confirmation
ZatcaMessage.Id=1
ZatcaMessage.Mode=0
ZatcaMessage.TopCaption.1=Zatca Issue
ZatcaMessage.SummaryInstruction.1=Zatca Issue
ZatcaMessage.EnableScanner=0
ZatcaMessage.HideHelp=1
ZatcaMessage.HideGoBack=1
ZatcaMessage.HideTotal=0
ZatcaMessage.TableName=POSMessages

---

VERSION 3.01.2.13.0 - 26-06-2023
================================
Implementation of Zatca B2B
Modified event.tbl - Added B2B button 0x008F
Added parameters:

1. zatca.properties:

// B2B Zatca enabling
b2b.enabled = true

// B2B URL to retrieve customer info
b2b.customer.url = https://91309fd1-2dff-4229-b073-e34294282e08.mock.pstmn.io/customerinfo

// B2B URL to send invoice
b2b.invoice.url = https://91309fd1-2dff-4229-b073-e34294282e08.mock.pstmn.io/customerinfo

// B2B client identification
b2b.sap-client.id = 100

// B2B Receipt lines - up to 9. If empty not printed
b2b.receipt.1 = ----------------------------------------
b2b.receipt.2 = Check your email. Your invoice will be
b2b.receipt.3 = sent as agreed in the upcoming hours
b2b.receipt.4 = ----------------------------------------

2. zatca-plugins.properties:
b2b-greencore.B2bSapPlugin=true

When B2B transaction is finalized:

1. Receipt is printed:
----------------------------------------
Check your email. Your invoice will be
sent as agreed in the upcoming hours
----------------------------------------

2. IDC is generated:
0106:010:230626:181206:2612:008:y:109:0000:                               1234

---