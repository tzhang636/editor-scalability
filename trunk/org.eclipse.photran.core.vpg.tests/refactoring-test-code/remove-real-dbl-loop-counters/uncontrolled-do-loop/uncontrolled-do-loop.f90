! DO loop selected for refactoring must be a controlled
! DO loop otherwise refactoring will not proceed.

PROGRAM UncontrolledDoLoop
  REAL :: counter, sum
  sum = 0.0
  counter = 1.2
  DO                                !<<<<< 8,3,8,5, 0, fail-initial
    sum = sum + counter
    counter = counter + 0.1
    IF (counter > 1.8) THEN
      EXIT
    END IF
  END DO
  PRINT *, sum
END PROGRAM UncontrolledDoLoop
