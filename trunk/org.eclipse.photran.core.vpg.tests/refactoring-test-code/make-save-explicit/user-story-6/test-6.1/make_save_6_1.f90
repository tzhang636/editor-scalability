! USER STORY 6, TEST 1
! Adds SAVE statement for variable call_counter due to variable being
! initialized in its declaration statement while sharing the same declaration
! statement with non-initialized variable other_var

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: call_counter = 0, other_var
  call_counter = call_counter + 1
  PRINT *, 'called:', call_counter
END SUBROUTINE MySub
