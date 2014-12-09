mathorium
=========
allow to create a syntax file based on regular expressions in order to parse a mathematical text
The part "read" start by analyzing atomic expressions which match regular expressions inside CDATA, 
then form complex expressions. Each expression well formed has a type.

The part "write" gives a readable form of the expression, there can be several group with a particular name like
"<write name="tex">" and so on.

The part "generators" is dedicated to rules producing new expressions from existing ones, one has to declare the
variables used, give them a type (name=..) and the replacement type (type=..)

Some syntax files are found in the folder Examples. To create a worksheet, right click a syntax file (ex : naturals.syx)
and select "new math file". You can open one of the existing math files in the same folder.
The button "exécution automatique" generate the expressions one by one. The process can be infinite, 
so you stop it by clicking on the progress bar's cross at the bottom.

The generators function also manually.
