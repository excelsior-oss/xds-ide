(*
* A sample source file for code formatter preview
*)                                      
                                        
MODULE Preview; 

IMPORT Strings; IMPORT Handlers; FROM Handlers IMPORT ExitHandler; 

CONST PI = 3.14159265; CONST CC = 10; TYPE TREC = RECORD F1 : INTEGER; CASE SEL: INTEGER OF | 1 :                          
             C1 : CHAR;                 
          | 2 :                         
             I1 : INTEGER; ELSE B : BOOLEAN; END; END; VAR a : TREC;    

MODULE LocalMod; EXPORT getPi; PROCEDURE getPi() : REAL; BEGIN RETURN PI; END getPi; BEGIN END LocalMod;                      
                                        
PROCEDURE Proc(); CONST CC = 10; TYPE TT = CHAR; VAR x : TT;

  PROCEDURE Local();                    
  BEGIN                                 
    x := a.C1;                             
  END Local;                            
                                        
BEGIN IF a.F1 = 1 THEN a.C1 := 'z'; CASE a.SEL OF | 1 :                            
         a.C1 := 'x'; | 2 :                            
         a.I1 := 3; ELSE a.F1 := 1;                     
       END; ELSF a.F1 = 2 THEN a.C1 := 'r'; ELSIF a.C1 := 'q'; END;                                
    Local();                            
END Proc; BEGIN Proc(); EXCEPT ExitHandler(false); FINALLY ExitHandler(true); END Preview.                            
