PROGRAM TEST_DERIVED_TYPE

	IMPLICIT NONE
	
	TYPE :: TestType
		INTEGER :: a, b
	END TYPE
	
	
	TYPE(TestType) :: TestVar
	
	
	TestVar%a = 10
	TestVar%b = 20

	CALL DISPLAYTEST(TestVar)

	
CONTAINS



	SUBROUTINE DISPLAYTEST(Var)
	
		IMPLICIT NONE
		
		TYPE(TestType), INTENT(IN) :: Var
		
		PRINT *, Var%a
		PRINT *, Var%b
		
	END SUBROUTINE



END PROGRAM