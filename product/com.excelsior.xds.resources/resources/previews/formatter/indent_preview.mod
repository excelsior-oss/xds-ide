(*
* A sample source file for code formatter preview
*)                                      
                                        
MODULE Preview;                         
                                        
TYPE TREC = RECORD                      
        F1 : INTEGER;                   
        CASE SEL: INTEGER OF 
          | 1 :                          
             C1 : CHAR;                 
          | 2 :                         
             I1 : INTEGER;              
        END;                            
    END;                                

VAR a : TREC;    

MODULE LocalMod;

PROCEDURE getPi() : REAL;
BEGIN
  RETURN 3.141592653;
END getPi;

BEGIN
END LocalMod;                       
                                        
PROCEDURE Proc(); 

VAR x : CHAR;      

  PROCEDURE Local();                    
  BEGIN                                 
    x := a.C1;                             
  END Local;                            
                                        
BEGIN                                   
    IF a.F1 = 1 THEN                    
       CASE (a.SEL) OF                    
       | 1 :                            
         a.C1 := 'x';                   
       | 2 :                            
         a.I1 := 3;                     
       ELSE                             
         a.F1 := 1;                     
       END;                             
    END;                                
    Local();                            
END Proc;                               
                                        
BEGIN                                   
    Proc();                             
END Preview.                            
