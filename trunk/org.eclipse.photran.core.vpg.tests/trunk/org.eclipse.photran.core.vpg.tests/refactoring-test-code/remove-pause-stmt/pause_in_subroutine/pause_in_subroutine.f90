! Checks for basic replacement of a PAUSE statement
! with PRINT and READ while PAUSE statement is in
! unit other than PROGRAM, e.g. SUBROUTINE.

PROGRAM PauseInSubroutine
  INTEGER :: i
  DO i = 1, 100
    IF (i == 50) THEN
      CALL CALLPRINT
    END IF
  END DO
  PRINT *, 'i=', i
END PROGRAM PauseInSubroutine


SUBROUTINE CALLPRINT
   PAUSE 'mid job'              !<<<<< 17, 4, 17, 19, pass
END
