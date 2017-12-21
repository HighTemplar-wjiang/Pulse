#define B1  0b00000001
#define LED 0b01000000
#define INT 100
#define BITS_ONCE 2
#define CHANNELX 1
#define CHANNELY 0
#define CHANNELZ 1
#define SEGMENT_LENGTH 4 // SEGMENT_LENGTH = CHANNELS * BITS_ONCE, MUST CHANGE IT IF OTHER VALUE CHANGED
#define CONDITIONS 16 // CONDITIONS = 1 << SEGMENT_LENGTH

#include <avr/io.h>
#include <util/delay.h>
#include <string.h>

void zeros(void);
void set_bit(uint8_t volatile *pPORT, int PIN, int bit);
void set_base(int flag);
void calibrate(void);
void sendSegment(const char bits[SEGMENT_LENGTH]);
void sendString(const char *str, int length);
void sendNote(const char *str);
void sendTest(const char *str);

int main(void)
{
    DDRA  |= 1 << PINA6; // LED 
	DDRA  &= ~(1 << PINA0); // button 1	
	DDRA  &= ~(1 << PINA1); // button 2
	DDRA  &= ~(1 << PINA2); // button 3
	DDRA  &= ~(1 << PINA3); // button 4
	DDRA  &= ~(1 << PINA4); // button 5
	
	// magnetism X
	DDRB |= 1 << PINB7; // output: EXT2 12
	DDRB |= 1 << PINB6; // output: EXT2 11
	DDRB |= 1 << PINB5; // output: EXT2 10
	DDRB |= 1 << PINB4; // output: EXT2 9 (base)
	
	// magnetism Y
	DDRD |= 1 << PIND7; // output: EXT1 14
	DDRD |= 1 << PIND6; // output: EXT1 13
	DDRD |= 1 << PIND5; // output: EXT1 12
	DDRD |= 1 << PIND4; // output: EXT1 11 (base)
	
	// magntism Z
	DDRE |= 1 << PINE7; // output: EXT1 8
	DDRE |= 1 << PINE6; // output: EXT1 7
	DDRE |= 1 << PINE5; // output: EXT1 6
	DDRE |= 1 << PINE4; // output: EXT1 5 (base)

	zeros();
	
	char string[] = "Hello world! This is a little interesting magnetic communication system. Though she is not perfect, she will change the world.";
	char note[] = "94-184x99-184x100-184x92-200x100-176x92-184x99-184x100-184x92-200x100-176x92-184x90-184x89-184x86-184x84-184x83-184x76-208x70-208x68-192x60-192x70-192x73-208x60-192x70-192x60-192x52-192x60-192x70-192x73-216x70-208x52-192x76-192x84-192x86-192x89-208x90-192x89-192x86-216x84-192x84-216x78-176x83-176x84-224x78-176x83-176x84-208x86-200x84-176x76-240x76-192x84-192x86-192x89-208x90-192x89-192x86-216x84-192x84-216x78-176x83-176x84-224x78-192x84-192x78-192x84-192x89-192x86-208x78-208x83-216x89-176x90-176x92-216x84-192x84-216x84-192x92-192x90-192x89-192x90-176x84-176x84-216x78-176x84-176x92-216x84-192x84-216x84-192x78-192x84-192x92-192x90-192x84-208x86-208x89-216x84-176x86-176x89-216x84-176x86-176x89-208x94-208x99-208x94-208x92-216x84-192x84-192x86-192x89-192x90-192x86-224x89-208x83-208x86-224x84-176x83-176x84-240x84-192x100-192x99-192x94-224x94-200x99-200x100-192x92-224x92-192x84-192x100-192x99-192x94-224x94-200x99-200x100-192x92-232x84-192x86-192x89-216x86-192x86-208x78-208x84-208x86-208x89-208x92-208x94-224x94-200x89-200x90-192x92-240x84-192x100-192x99-192x94-216x94-192x94-200x99-200x100-192x92-224x92-192x84-192x100-192x99-192x94-224x94-200x99-200x100-192x105-232x84-192x86-192x89-216x86-192x86-208x78-192x86-192x89-208x86-208x78-208x83-208x84-240x76-192x84-192x92-192x92-208x90-208x84-208x86-208x84-240x";
	char test[] = "102,210,083,015,160,127,132,108,209,187,222,084,079,234,175,030,180,011,013,207,104,072,035,087,065,093,253,178,138,175,251,065,077,038,212,144,107,065,198,015,075,168,239,206,125,182,072,079,103,201,196,121,077,134,057,247,000,055,133,190,107,151,102,156,014,114,193,017,136,071,005,188,047,049,182,085,072,224,085,253,024,198,229,199,199,046,151,212,073,133,038,188,174,144,132,161,216,203,057,025,226,057,117,159,215,105,209,205,019,180,255,207,112,062,115,117,237,244,035,056,096,066,242,073,217,048,208,244,191,109,098,173,068,167,116,115,252,242,046,008,161,106,174,107,024,100,247,052,126,006,094,172,076,245,091,217,061,246,247,076,009,253,235,129,157,178,122,189,094,081,102,150,145,208,232,037,056,067,054,053,115,108,183,227,175,100,011,055,119,201,126,222,020,083,117,149,247,091,229,110,169,015,111,130,226,147,198,050,245,160,218,156,004,182,024,022,202,164,179,162,213,223,007,197,129,078,255,254,058,095,167,216,117,020,163,019,213,162,169,055,050,245,215,144,179,057,016,065,062,096,114,136,184,169,178,100,";
	char test2[] = "\0ABC";
	
	
	//_delay_ms(5000);	
	while(1)
	{
		set_bit(&PORTE, PINE5, 1);		
	    if (bit_is_clear(PINA, 0))
		{
			PORTA |= 1 << PINA6; // LED on
			set_base(1);
			//sendString(string, -1);
			sendString(test2, 4);
			set_base(0);
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 1))
		{
		    PORTA |= 1 << PINA6; // LED on
			set_base(1);
			_delay_ms(5000);
			set_base(0);
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 2))
		{
		    PORTA |= 1 << PINA6; // LED on
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 3))
		{
		    PORTA |= 1 << PINA6; // LED on
			set_base(1);
			//sendNote(note);
			sendTest(test);
			set_base(0);
			PORTA &= ~(1 << PINA6); // LED off
		}
		
		if (bit_is_clear(PINA, 4))
		{
		    PORTA |= 1 << PINA6; // LED on
			calibrate();// calibrate
			PORTA &= ~(1 << PINA6); // LED off
		}
	}
}

void set_bit(uint8_t volatile *pPORT, int PIN, int bit)
{
    if(bit == 0)
	{
	    *pPORT &= ~(1 << PIN);
	}
	
	if(bit == 1)
	{
	    *pPORT |= 1 << PIN;
	}
}

// set all output to zero
void zeros(void)
{
    PORTB &= ~(1 << PINB7);
	PORTB &= ~(1 << PINB6);
	PORTB &= ~(1 << PINB5);
	//PORTB &= ~(1 << PINB4);
	
	PORTD &= ~(1 << PIND7);
	PORTD &= ~(1 << PIND6);
	PORTD &= ~(1 << PIND5);
	//PORTD &= ~(1 << PIND4);
	
	PORTE &= ~(1 << PINE7);
	PORTE &= ~(1 << PINE6);
	PORTE &= ~(1 << PINE5);
	//PORTE &= ~(1 << PINE4);
}

void set_base(int flag)
{
    if(flag == 0)
	{
	    set_bit(&PORTB, PINB4, 0);
		set_bit(&PORTD, PIND4, 0);
		set_bit(&PORTE, PINE4, 0);
	}
	
	if(flag == 1)
	{
	    set_bit(&PORTB, PINB4, 1);
		set_bit(&PORTD, PIND4, 1);
		set_bit(&PORTE, PINE4, 1);
	}
}

void calibrate(void)
{
	int i, j;
	char segment[SEGMENT_LENGTH];
	
    _delay_ms(1000);
	set_base(1);
	_delay_ms(INT * 2);
	for(i = 0; i < CONDITIONS; i++)
	{
		for(j = 0; j < SEGMENT_LENGTH; j++)
		{
		    segment[j] = (i >> (SEGMENT_LENGTH - j - 1)) & 0x01;
		}
		sendSegment(segment);
		_delay_ms(200);
	}
	//_delay_ms(INT * 2);
	zeros();
	set_base(0);
}

void sendSegment(const char bits[SEGMENT_LENGTH])
{
	int iPosition = 0;
	int iCount = BITS_ONCE;
	if(CHANNELX == 1)
	{
		//if(iCount-- > 0) set_bit(&PORTB, PINB4, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTB, PINB7, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTB, PINB6, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTB, PINB5, bits[iPosition++]);
	}
	
	iCount = BITS_ONCE;
	if(CHANNELY == 1)
	{
		//if(iCount-- > 0) set_bit(&PORTD, PIND4, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTD, PIND7, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTD, PIND6, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTD, PIND5, bits[iPosition++]);
	}
	
	iCount = BITS_ONCE;
	if(CHANNELZ == 1)
	{
		//if(iCount-- > 0) set_bit(&PORTE, PINE4, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTE, PINE7, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTE, PINE6, bits[iPosition++]);
		if(iCount-- > 0) set_bit(&PORTE, PINE5, bits[iPosition++]);		
	}
}

void sendString(const char *str, int len)
{
    char segment_buffer[SEGMENT_LENGTH];
	char bits_buffer[SEGMENT_LENGTH * 8];
    
	int length = strlen(str);
	int iSentCount = 0;
	
	if(len != -1)
	{
	    length = len;
	}
	
	int x;
	for(x = 0; x < SEGMENT_LENGTH * 8; x++)
	{
	    bits_buffer[x] = '\0';
	}
	
	while(iSentCount < length)
	{
	    int i = 0; // get SEGMENT_LENGTH characters once
		int j = 0;
		char current = '\0';
	    while(i < SEGMENT_LENGTH)
		{
		    if(str[iSentCount] != '\0' || len != 0)
			{
			    current = str[iSentCount++];
			}
			else
			{
			    current = '\0';
			}
						
			for(j = 0; j < 8; j++)
			{
			    bits_buffer[i * 8 + j] = (current >> (7 - j)) & 0x01;
			}
		    i++;
		}
		set_base(1);
		for(i = 0; i < 8; i++)
		{
		    for(j = 0; j < SEGMENT_LENGTH; j++)
			{
			    segment_buffer[j] = bits_buffer[i * SEGMENT_LENGTH + j];
			}
			sendSegment(segment_buffer);
			_delay_ms(INT);
		}
		zeros();
		set_base(0);
		_delay_ms(INT);
	}
}

void sendNote(const char *str)
{
    int iSegment;
	int iBuffer;
    char segment_buffer[SEGMENT_LENGTH];
	char buffer[5];
	
	int i = 0;
	int iPos = 0;
	int one = 0;
	
	for(i = 0; i < 4; i++)
	{
	    buffer[i] = '\0';
	}
	
	i = 0;
	iPos = 0;
	one = 0;
	iSegment = 0;
	iBuffer = 0;
	for(iPos = 0; str[iPos] != '\0'; iPos++)
	{
		buffer[iBuffer++] = str[iPos];
		
	    if(str[iPos] == '-')
		{
		    one = atoi(buffer);
			if(one == 0)
			{
			    one = 63;
			}
			segment_buffer[iSegment++] = one & 0x7F;
		    for(i = 0; i < 5; i++)
			{
			    buffer[i] = '\0';
			}
			iBuffer = 0;
		}
		
		if(str[iPos] == 'x')
		{
			one = atoi(buffer);
			segment_buffer[iSegment++] = one & 0xFF;
		    for(i = 0; i < 5; i++)
			{
			    buffer[i] = '\0';
			}
			iBuffer = 0;
		}
		
		if(iSegment == SEGMENT_LENGTH) {
		    sendString(segment_buffer, SEGMENT_LENGTH);
			iSegment = 0;
			iBuffer = 0;
		}
		
	
	}
	
}

void sendTest(const char *str)
{
    int i;
    int iPos;
	int iSegment;
	int iBuffer;
	char buffer[5];
	char segment[SEGMENT_LENGTH];
	char one = 0;
	
	for(iPos = 0; iPos < 5; iPos++)
	{
	    buffer[iPos] = '\0';
	}
	
	iSegment = 0;
	iBuffer = 0;
	for(iPos = 0; str[iPos] != '\0'; iPos++)
	{
	    if(str[iPos] == ',')
		{
		    one = atoi(buffer);
			segment[iSegment++] = one;
			for(i = 0; i < 5; i++)
			{
				buffer[i] = '\0';
			}
			iBuffer = 0;
		}
		else
		{
		    buffer[iBuffer++] = str[iPos];
		}
		
		if(iSegment == SEGMENT_LENGTH)
		{
		    sendString(segment, SEGMENT_LENGTH);
			iSegment = 0;
		}
	}
}


