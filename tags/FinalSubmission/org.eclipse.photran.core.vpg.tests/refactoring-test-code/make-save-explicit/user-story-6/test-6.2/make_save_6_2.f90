! USER STORY 6, TEST 2
! Adds variable call_counter to a previously existing SAVE statement due to
! variable being initialized in its declaration statement while sharing the
! same declaration statement with non-initialized variable other_var

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  SAVE third_var
  INTEGER :: call_counter = 0, other_var
  INTEGER :: third_var
  call_counter = call_counter + 1
  PRINT *, 'called:', call_counter
END SUBROUTINE MySub
