! USER STORY 7, TEST 1
! Adds SAVE statement for variable call_counter due to variable being
! implicitly saved in a data block

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER call_counter
  data call_counter /10/
  call_counter = call_counter + 1
  PRINT *, 'called:', call_counter
END SUBROUTINE MySub
