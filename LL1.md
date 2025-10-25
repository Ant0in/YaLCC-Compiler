# LL(1) Parsing Table

```sh
Non-Terminal   | Token(s) (Terminal)                        | Production                            | Rule #
---------------|------------------------------------------- |---------------------------------------|---------
Program        | PROG                                       | Prog PROGNAME Is Code End             | 1
               |                                            |                                       |
Code           | VARNAME, IF, WHILE, PRINT, INPUT           | Instruction ; Code                    | 2
Code           | END                                        | ε                                     | 3
               |                                            |                                       |
Instruction    | VARNAME                                    | Assign                                | 4
Instruction    | IF                                         | If                                    | 5
Instruction    | WHILE                                      | While                                 | 6
Instruction    | PRINT                                      | Output                                | 7
Instruction    | INPUT                                      | Input                                 | 8
               |                                            |                                       |
Assign         | VARNAME                                    | VarName = ExprArith                   | 9
               |                                            |                                       |
If             | IF                                         | If { Cond } Then Code End             | 10
If             | IF                                         | If { Cond } Then Code Else Code End   | 11
               |                                            |                                       |
While          | WHILE                                      | While { Cond } Do Code End            | 12
               |                                            |                                       |
Output         | PRINT                                      | Print ( VarName )                     | 13
Input          | INPUT                                      | Input ( VarName )                     | 14
               |                                            |                                       |
Cond           | PIPE, VARNAME, NUMBER, LPAREN, MINUS       | CondImpl                              | 15
               |                                            |                                       |
CondImpl       | PIPE, VARNAME, NUMBER, LPAREN, MINUS       | CondAtom -> CondImpl                  | 16
CondImpl       | PIPE, VARNAME, NUMBER, LPAREN, MINUS       | CondAtom                              | 16*
               |                                            |                                       |
CondAtom       | PIPE                                       | | Cond |                              | 17
CondAtom       | VARNAME, NUMBER, LPAREN, MINUS             | CondComp                              | 18
               |                                            |                                       |
CondComp       | VARNAME, NUMBER, LPAREN, MINUS             | ExprArith Comp ExprArith              | 19
               |                                            |                                       |
ExprArith      | VARNAME, NUMBER, LPAREN, MINUS             | ExprAddSub                            | 20
ExprAddSub     | VARNAME, NUMBER, LPAREN, MINUS             | ExprMulDiv { (+|-) ExprMulDiv }*      | 21
ExprMulDiv     | VARNAME, NUMBER, LPAREN, MINUS             | ExprUnary { (*|/) ExprUnary }*        | 22
ExprUnary      | MINUS                                      | - ExprPrimary                         | 23
ExprUnary      | VARNAME, NUMBER, LPAREN                    | ExprPrimary                           | 24
ExprPrimary    | VARNAME                                    | VarName                               | 25
ExprPrimary    | NUMBER                                     | Number                                | 26
ExprPrimary    | LPAREN                                     | ( ExprArith )                         | 27
```
